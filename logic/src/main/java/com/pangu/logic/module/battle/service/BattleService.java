package com.pangu.logic.module.battle.service;

import com.pangu.framework.resource.Storage;
import com.pangu.framework.resource.anno.Static;
import com.pangu.framework.utils.ManagedException;
import com.pangu.framework.utils.json.JsonUtils;
import com.pangu.logic.module.battle.model.*;
import com.pangu.logic.module.battle.model.report.FightReport;
import com.pangu.logic.module.battle.resource.BattleSetting;
import com.pangu.logic.module.battle.service.convertor.Battler;
import com.pangu.logic.module.battle.service.convertor.FighterConvertor;
import com.pangu.logic.module.battle.service.core.Fighter;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.formation.model.FormationType;
import com.pangu.logic.module.formation.model.FormationTypeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 战斗服务
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@Service("newBattleServices")
@Slf4j
public class BattleService {

    @Autowired
    private List<FighterConvertor<?>> convertList;

    @Static
    private Storage<BattleType, BattleSetting> battleSettingStorage;

    // 常规战斗配置
    private BattleSetting normalBattleConfig;

    //  不同的战斗单位转换器
    private final ConcurrentHashMap<FighterType, FighterConvertor<?>> converts = new ConcurrentHashMap<>(FighterType.values().length);

    //  初始化方法
    @SuppressWarnings("rawtypes")
    @PostConstruct
    public void initialize() {
        // 初始化战斗单位转换器信息
        for (FighterConvertor convert : convertList) {
            if (converts.put(convert.getType(), convert) != null) {
                throw new IllegalStateException("战斗单位的转换器重复" + convert.getType());
            }
        }
        convertList.clear();
        convertList = null;
        normalBattleConfig = battleSettingStorage.get(BattleType.NORMAL, true);
    }

    public FightResult start(long id, BattleType type, FormationType formation, String enemyId) {
        return start(id, type, formation, enemyId, type.name());
    }

    public FightResult start(long id, BattleType type, FormationType formation, String enemyId, String sceneId) {
        Battler<FormationTypeUnit> attacker = Battler.valueOf(FighterType.PLAYER, new FormationTypeUnit(id, formation));
        Battler<String> defender = Battler.valueOf(FighterType.ENEMY, enemyId);
        return start(type, attacker, defender, sceneId);
    }

    /**
     * 开始战斗
     *
     * @param type     战斗类型
     * @param attacker 攻击方
     * @param defender 防守方
     * @return
     */
    @SuppressWarnings("rawtypes")
    public FightResult start(BattleType type, Battler attacker, Battler defender) {
        return start(type, attacker, defender, type.name());
    }

    public FightResult start(BattleType type, Battler attacker, Battler defender, String sceneId) {
        int attackerTimes = getBattleTimes(attacker, attacker.getContent());

        int defenderTimes = getBattleTimes(defender, defender.getContent());

        if (attackerTimes < defenderTimes) {
            throw new IllegalStateException("战场队伍数量，攻击方与防守方数量不一致[" + attacker.getContent() + ":" + defender.getContent() + "]");
        }

        int times = Math.min(attackerTimes, defenderTimes);
        final int winTimes = getWinTimes(type, defender, defender.getContent());
        List<Fighter> attackers = new ArrayList<>(times);
        List<Fighter> defenders = new ArrayList<>(times);
        for (int i = 0; i < times; ++i) {
            // 构建战斗对象
            Fighter aFighter = transform(attacker, true, i);
            Fighter dFighter = transform(defender, false, i);
            attackers.add(aFighter);
            defenders.add(dFighter);
        }

        return start(type, attackers, defenders, winTimes, sceneId);
    }


    /**
     * 开始战斗(多场)
     *
     * @param type      战斗类型
     * @param attackers 攻击方
     * @param defenders 防守方
     * @param winTimes  需要胜利几场才属于胜利
     * @return
     */
    public FightResult start(BattleType type, List<Fighter> attackers, List<Fighter> defenders, int winTimes) {
        return start(type, attackers, defenders, winTimes, type.name());
    }

    public FightResult start(BattleType type, List<Fighter> attackers, List<Fighter> defenders, int winTimes, String sceneId) {
        final BattleSetting battleSetting = getBattleSetting(type);
        if (!battleSetting.isLazyResult()) {
            return hungryResult(type, attackers, defenders, winTimes, sceneId);
        }

        final int size = attackers.size();
        if (size != defenders.size()) {
            throw new IllegalStateException(type.name() + "战场队伍数量，攻击方与防守方数量不一致[" + size + ":" + defenders.size() + "]");
        }
        List<Battle> battles = new ArrayList<>(size);
        int times = 0;
        for (int i = 0; i < size; i++) {
            final Fighter attacker = attackers.get(i);
            final Fighter defender = defenders.get(i);

            Battle battle = battle(type, sceneId, attacker, defender);

            battles.add(battle);
            if (battle.getResult() == BattleResult.ATTACKER) {
                ++times;
            }
        }
        if (times >= winTimes) {
            return FightResult.of(BattleResult.ATTACKER, battles);
        }
        return FightResult.of(BattleResult.DEFENDER, battles);
    }

    private FightResult hungryResult(BattleType type, List<Fighter> attackers, List<Fighter> defenders, int winLine, String sceneId) {
        final int size = attackers.size();
        if (size != defenders.size()) {
            throw new IllegalStateException(type.name() + "战场队伍数量，攻击方与防守方数量不一致[" + size + ":" + defenders.size() + "]");
        }
        List<Battle> battles = new ArrayList<>(size);
        int winGap = winLine;
        int chances = size;
        for (int i = 0; i < size; i++) {
            
            if (chances < winGap) {
                break;
            }

            final Fighter attacker = attackers.get(i);
            final Fighter defender = defenders.get(i);
            Battle battle = battle(type, sceneId, attacker, defender);

            if (battle.getResult() == BattleResult.ATTACKER) {
                --winGap;
            }
            --chances;
            battles.add(battle);

            
            if (winGap == 0) {
                return FightResult.of(BattleResult.ATTACKER, battles);
            }
        }
        return FightResult.of(BattleResult.DEFENDER, battles);
    }

    private Battle battle(BattleType type, String sceneId, Fighter attacker, Fighter defender) {
        Battle battle;
        if (attacker == null || attacker.isEmpty()) {
            battle = Battle.valueOfNoAttacker(defender, type, sceneId);
        } else if (defender == null || defender.isEmpty()) {
            battle = Battle.valueOfNoDefender(attacker, type, sceneId);
        } else {
            battle = startBattle(type, attacker, defender, sceneId);
        }
        return battle;
    }

    /**
     * 开始战斗
     *
     * @param type     战斗类型
     * @param attacker 攻击方
     * @param defender 防守方
     * @param times    战斗次数
     * @param winTimes 胜利次数
     * @return
     */
    @SuppressWarnings("rawtypes")
    public FightResult startFixedTimes(BattleType type, Battler attacker, Battler defender, int times, int winTimes) {
        return startFixedTimes(type, attacker, defender, times, winTimes, type.name());
    }

    public FightResult startFixedTimes(BattleType type, Battler attacker, Battler defender, int times, int winTimes, String sceneId) {
        List<Fighter> attackers = new ArrayList<>(times);
        List<Fighter> defenders = new ArrayList<>(times);
        for (int i = 0; i < times; ++i) {
            // 构建战斗对象
            Fighter aFighter = transform(attacker, true, i);
            Fighter dFighter = transform(defender, false, i);
            attackers.add(aFighter);
            defenders.add(dFighter);
        }

        return start(type, attackers, defenders, winTimes, sceneId);
    }

    /**
     * 开始战斗(单场)
     *
     * @param type     战斗类型
     * @param attacker 攻击方
     * @param defender 防守方
     * @return
     */
    public FightResult startSingle(BattleType type, Fighter attacker, Fighter defender) {
        return startSingle(type, attacker, defender, type.name());
    }

    public FightResult startSingle(BattleType type, Fighter attacker, Fighter defender, String sceneId) {
        final Battle battle = startBattle(type, attacker, defender, sceneId);
        return FightResult.of(battle.getResult(), battle);
    }

    private Battle startBattle(BattleType type, Fighter attacker, Fighter defender, String sceneId) {
        if (StringUtils.isEmpty(sceneId)) {
            sceneId = type.name();
        }
        if (attacker == null) {
            throw new ManagedException(com.pangu.logic.module.battle.facade.BattleResult.ATTACKER_NOT_FOUND);
        }
        if (defender == null) {
            throw new ManagedException(com.pangu.logic.module.battle.facade.BattleResult.DEFENDER_NOT_FOUND);
        }
        BattleSetting config = getBattleSetting(type);
        // 创建战斗对象
        Battle battle = Battle.valueOf(type, config, attacker, defender, sceneId);
        try {
            battle.start();
        } catch (Exception e) {
            log.error("战斗异常[{}][{}]", JsonUtils.object2String(FighterInfo.valueOf(attacker)), JsonUtils.object2String(FighterInfo.valueOf(defender)), e);
            throw new ManagedException(com.pangu.logic.module.battle.facade.BattleResult.PROCESS_ERROR, e);
        }
        // 解析并设置战斗战报
        final FightReport report = battle.getReport();
        Map<String, String> summerMap = summer(battle);
        report.summary(summerMap);

        return battle;
    }

    private Map<String, String> summer(Battle battle) {
        Fighter attacker = battle.getAttacker();
        Map<String, String> summer = new HashMap<>();
        searchSummer(summer, attacker.getCurrent());
        searchSummer(summer, attacker.getDieUnit());
        Fighter defender = battle.getDefender();
        searchSummer(summer, defender.getCurrent());
        searchSummer(summer, defender.getDieUnit());
        return summer;
    }

    private void searchSummer(Map<String, String> summer, List<Unit> current) {
        if (current == null) {
            return;
        }
        for (Unit unit : current) {
            if (!unit.isSummon()) {
                continue;
            }

            Unit summonUnit = unit.getSummonUnit();
            if (summonUnit != null) {
                summer.put(unit.getId(), summonUnit.getId());
            }
        }
    }

    private BattleSetting getBattleSetting(BattleType type) {
        BattleSetting config = battleSettingStorage.get(type, false);
        if (config == null) {
            config = normalBattleConfig;
        }
        return config;
    }

    /**
     * 将{@link Battler}转换为{@link Fighter}
     *
     * @param battler    作战标识
     * @param isAttacker 攻击方/防守方
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Fighter transform(Battler battler, boolean isAttacker, int index) {
        FighterType type = battler.getType();
        FighterConvertor convertor = converts.get(type);
        if (convertor == null) {
            FormattingTuple message = MessageFormatter.format("战斗单位类型[{}]的转换器不存在", type);
            log.error(message.getMessage());
            throw new ManagedException(com.pangu.logic.module.battle.facade.BattleResult.CONFIG_ERROR, message.getMessage());
        }
        return convertor.convert(battler.getContent(), isAttacker, index);
    }

    private int getBattleTimes(Battler<?> attacker, Object content) {
        FighterType fighterType = attacker.getType();
        FighterConvertor attackerConvert = converts.get(fighterType);
        return attackerConvert.getBattleTimes(content);
    }

    private int getWinTimes(BattleType type, Battler attacker, Object content) {
        FighterType fighterType = attacker.getType();
        FighterConvertor convertor = converts.get(fighterType);
        final Integer winTimes = convertor.getWinTimes(content);
        if (winTimes != null) {
            return winTimes;
        }
        final BattleSetting config = getBattleSetting(type);
        if (config == null) {
            return getBattleTimes(attacker, attacker.getContent());
        }
        return config.getWinTimes();
    }
}
