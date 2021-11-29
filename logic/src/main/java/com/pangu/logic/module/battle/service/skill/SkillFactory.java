package com.pangu.logic.module.battle.service.skill;

import com.pangu.logic.module.battle.facade.BattleResult;
import com.pangu.logic.module.battle.model.BattleType;
import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.resource.EffectSetting;
import com.pangu.logic.module.battle.resource.FightSkillSetting;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.Battle;
import com.pangu.logic.module.battle.service.action.Action;
import com.pangu.logic.module.battle.service.action.SkillAction;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.Fighter;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.framework.resource.Storage;
import com.pangu.framework.resource.anno.Static;
import com.pangu.framework.utils.ManagedException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 主动技能工厂
 */
@Component
@Slf4j
public class SkillFactory {

    @Static
    private Storage<String, FightSkillSetting> skillStorage;
    @Static
    private Storage<String, EffectSetting> effectStorage;
    @Static
    private Storage<String, SelectSetting> selectStorage;

    @Autowired
    private Collection<SkillEffect> skillEffects;

    private final SkillEffect[] effects = new SkillEffect[EffectType.values().length];

    private static SkillFactory INSTANCE;

    //  初始化方法
    @PostConstruct
    protected void init() {
        // 初始化技能效果实例
        for (SkillEffect skillEffect : skillEffects) {
            EffectType type = skillEffect.getType();
            if (effects[type.ordinal()] != null) {
                throw new IllegalStateException("主动技能效果类型实例重复[" + type + "]");
            }
            effects[type.ordinal()] = skillEffect;
        }
        SkillFactory.INSTANCE = this;
        skillEffects.clear();
        skillEffects = null;
    }

    public static SkillEffect getSkillEffect(String effectId) {
        return INSTANCE.getSkillEffect0(effectId);
    }

    public static void updateNextExecuteSkill(int time, Unit unit, String skillId) {
        SkillState skillState = initState(skillId);
        updateNextExecuteSkill(time, unit, skillState);
    }

    public static void updateNextExecuteSkill(int time, Unit unit, SkillState skillState) {
        // 添加保存技能战报
        Unit target = unit.getTarget();
        SkillReport skillReport = SkillReport.sing(time, unit.getId(), skillState.getId(), skillState.getSingTime(), (target == null || target.isDead()) ? null : target.getId());
        Battle battle = unit.getBattle();
        battle.addReport(skillReport);

        SkillAction skillAction = new SkillAction(time + skillState.getSingTime(), unit, skillState, skillReport, skillState.getSingAfterDelay());

        Action action = unit.getAction();
        if (action instanceof SkillAction) {
            ((SkillAction) action).broken(time);
        }
        unit.updateAction(skillAction);
        if (skillState.getSingTime() <= 0) skillAction.execute();
        battle.unitActionUpdate(unit);
    }

    public static void updateSpace(int time, Unit unit, SkillState skillState) {
        Battle battle = unit.getBattle();
        BattleType type = battle.getType();
        Fighter friend = unit.getFriend();
        battle.addSpaceCD(time);
        int singTime = skillState.getSingTime();
        if (!battle.isPause() && friend != null && !friend.isAttacker()) {
            singTime += skillState.getPauseTime();
        }

        // 添加保存技能战报
        Unit target = unit.getTarget();
        SkillReport skillReport = SkillReport.sing(time, unit.getId(), skillState.getId(), singTime, (target == null || target.isDead()) ? null : target.getId());
        battle.addReport(skillReport);

        SkillAction skillAction = new SkillAction(time + singTime, unit, skillState, skillReport, skillState.getSingAfterDelay());

        Action action = unit.getAction();
        if (action instanceof SkillAction) {
            ((SkillAction) action).broken(time);
        }
        unit.updateAction(skillAction);
        if (singTime <= 0) skillAction.execute();
        battle.unitActionUpdate(unit);
    }

    SkillEffect getSkillEffect0(String effectId) {
        EffectSetting config = effectStorage.get(effectId, true);
        SkillEffect result = effects[config.getType().ordinal()];
        if (result == null) {
            log.error("类型为[{}]主动技能效果不存在", config.getType());
            throw new ManagedException(BattleResult.CONFIG_ERROR);

        }
        return result;
    }

    public static SkillEffect getSkillEffect(EffectType type) {
        return INSTANCE.getSkillEffect0(type);
    }

    SkillEffect getSkillEffect0(EffectType type) {
        SkillEffect result = effects[type.ordinal()];
        if (result == null) {
            log.error("类型为[{}]主动技能效果不存在", type);
            throw new ManagedException(BattleResult.CONFIG_ERROR);
        }
        return result;
    }

    public static SkillState initState(String id) {
        return INSTANCE.initState0(id);
    }

    SkillState initState0(String id) {
        FightSkillSetting skillSetting = skillStorage.get(id, false);
        if (skillSetting == null) {
            log.error("标识为[{}]主动技能配置不存在", id, new IllegalArgumentException());
            throw new ManagedException(BattleResult.CONFIG_ERROR);
        }
        int range = Integer.MAX_VALUE;
        String[] effects = skillSetting.getEffects();
        List<EffectState> effectStates = new ArrayList<>(effects.length);
        for (String effectId : effects) {
            EffectSetting effectSetting = effectStorage.get(effectId, true);

            String target = effectSetting.getTarget();
            SelectSetting selectSetting = selectStorage.get(target, true);
            int distance = selectSetting.getDistance();
            if (distance > 0 && distance < range) {
                range = distance;
            }
            EffectState effectState = new EffectState(effectSetting, distance);
            effectStates.add(effectState);
        }
        range = range == Integer.MAX_VALUE ? 0 : range;
        return new SkillState(skillSetting, effectStates, range);
    }

    /**
     * 获取效果状态
     *
     * @param effectId
     * @return
     */
    public static EffectState getEffectState(String effectId) {
        return INSTANCE.getEffectState0(effectId);
    }

    EffectState getEffectState0(String effectId) {
        EffectSetting effectSetting = effectStorage.get(effectId, true);
        String target = effectSetting.getTarget();

        int distance = 0;
        if (StringUtils.isNotEmpty(target)) {
            SelectSetting selectSetting = selectStorage.get(target, true);
            distance = selectSetting.getDistance();
        }
        return new EffectState(effectSetting, distance);
    }

}
