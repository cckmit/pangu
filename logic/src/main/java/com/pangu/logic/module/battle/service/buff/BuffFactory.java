package com.pangu.logic.module.battle.service.buff;

import com.pangu.logic.module.battle.facade.BattleResult;
import com.pangu.logic.module.battle.model.UnitRate;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.values.BuffAdd;
import com.pangu.logic.module.battle.model.report.values.Immune;
import com.pangu.logic.module.battle.resource.BuffSetting;
import com.pangu.logic.module.battle.service.Battle;
import com.pangu.logic.module.battle.service.action.Action;
import com.pangu.logic.module.battle.service.action.BuffAction;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.common.resource.Formula;
import com.pangu.framework.resource.Storage;
import com.pangu.framework.resource.anno.Static;
import com.pangu.framework.utils.ManagedException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * BUFF效果工厂
 */
@Component
@Slf4j
public class BuffFactory {

    @Static
    private Storage<String, BuffSetting> configs;

    @Static
    private Storage<String, Formula> formulaStorage;

    @Autowired
    private List<Buff> buffsList;

    //  BUFF效果实例
    private final Buff[] buffs = new Buff[BuffType.values().length];

    private static BuffFactory INSTANCE;

    //  初始化方法
    @PostConstruct
    protected void init() {
        for (Buff buff : buffsList) {
            BuffType type = buff.getType();
            if (buffs[type.ordinal()] != null) {
                throw new IllegalStateException("BUFF效果类型实例重复" + buff.getType());
            }
            buffs[type.ordinal()] = buff;
        }
        BuffFactory.INSTANCE = this;
    }

    public static BuffState addBuff(String buffId, Unit owner, Unit target, int time, ITimedDamageReport timedDamageReport, Object addition) {
        if (StringUtils.isEmpty(buffId)) {
            return null;
        }
        BuffReport buffReport = BuffReport.of(time, target.getId(), owner.getId(), buffId);
        BuffState buffState = INSTANCE.initState(owner, target, buffId, buffReport, addition);
        return addBuff(buffState, time, target, timedDamageReport);
    }

    public static BuffState addBuff(BuffSetting buffSetting, Unit owner, Unit target, int time, ITimedDamageReport timedDamageReport, Object addition) {
        final String buffId = buffSetting.getId();
        BuffReport buffReport = BuffReport.of(time, target.getId(), owner.getId(), buffId);
        BuffState buffState = INSTANCE.initState(owner, target, buffSetting, buffReport, addition);
        return addBuff(buffState, time, target, timedDamageReport);
    }

    private static BuffState addBuff(BuffState buffState, int time, Unit target, @NonNull ITimedDamageReport skillReport) {
        Action buffAddAction = new Action() {
            @Override
            public int getTime() {
                return time;
            }

            @Override
            public void execute() {
                if (target.isDead()) {
                    return;
                }
                BuffSetting buffSetting = buffState.getBuffSetting();

                final Battle battle = target.getBattle();
                if (buffSetting.isRepeat()) {
                    buffState.setCustomTag(battle.createBuffTag());
                } else {
                    String tag = buffState.getTag();
                    BuffState preTagBuff = target.getBuffStateByTag(tag);
                    if (preTagBuff != null) {
                        if (preTagBuff != whoShouldBeOverwritten(preTagBuff, buffState)) {
                            return;
                        }
                        doRemoveBuffState(preTagBuff, target, time);
                    }
                }
                if (target.isDead()) {
                    return;
                }

                Buff buff = getBuff(buffState.getType());

                final int validTime = buffState.getTime();
                boolean success = isBuffUpdateSuccess(buffState, time, target)
                        && validTime > 0
                        && buff.add(buffState, target, time);
                buffState.setSuccess(success);

                /* 添加失败 */
                if (!success) {
                    skillReport.add(time, target.getId(), new Immune());
                    return;
                }

                String buffId = buffSetting.getId();
                final int index = battle.buffIndexIncr();

                final String caster = buffState.getCaster().getId();
                if (caster.equals(skillReport.getOwner())) {
                    skillReport.add(time, target.getId(), BuffAdd.of(buffId, caster, index));
                }

                final BuffReport buffReport = buffState.getBuffReport();
                buffReport.setIndex(index);
                battle.addReport(buffReport);

                int interval = buffSetting.getInterval();
                int nextActionTime = time + validTime;
                if (interval > 0) {
                    // 循环执行
                    int nextIntervalTime = time + interval;
                    if (nextIntervalTime < nextActionTime) {
                        nextActionTime = nextIntervalTime;
                    }
                }
                BuffAction buffAction = new BuffAction(nextActionTime, buffState, target, time + validTime);
                buffState.setBuffAction(buffAction);
                target.addTimedAction(buffAction);
            }
        };
        target.addTimedAction(buffAddAction);
        return buffState;
    }

    public static void removeBuffState(String buffId, Unit unit, int time) {
        BuffSetting setting = getSetting(buffId);
        String tag = setting.getTag();
        BuffState buffState = unit.getBuffStateByTag(tag);
        if (buffState == null) {
            return;
        }
        removeBuffState(buffState, unit, time);
    }

    public static void removeBuffState(BuffState buffState, Unit unit, int time) {
        if (unit.isDead()) {
            return;
        }
        Action removeAction = new Action() {
            @Override
            public int getTime() {
                return time;
            }

            @Override
            public void execute() {
                doRemoveBuffState(buffState, unit, time);
            }
        };
        unit.addTimedAction(removeAction);
    }

    public static void doRemoveBuffState(BuffState buffState, Unit unit, int time) {
        if (unit.isDead()) {
            return;
        }
        BuffType type = buffState.getType();
        Buff buff = getBuff(type);

        buff.remove(buffState, unit, time);

        buffState.getBuffReport().remove(time);

        BuffAction buffAction = buffState.getBuffAction();
        if (buffAction != null) {
            buffAction.done();
        }
    }

    public static void removeBuffStates(Collection<BuffState> buffStates, Unit unit, int time) {
        for (BuffState buffState : buffStates) {
            removeBuffState(buffState, unit, time);
        }
    }

    public static Buff getBuff(BuffType type) {
        return INSTANCE.getBuff0(type);
    }

    public Buff getBuff0(BuffType type) {
        Buff result = buffs[type.ordinal()];
        if (result == null) {
            FormattingTuple message = MessageFormatter.format("BUFF效果类型[{}]的实例不存在", type);
            log.error(message.getMessage());
            throw new ManagedException(BattleResult.CONFIG_ERROR);
        }
        return result;
    }

    public static BuffSetting getSetting(String buffId) {
        return INSTANCE.configs.get(buffId, true);
    }

    private BuffState initState(Unit caster, Unit target, String id, BuffReport buffReport, Object addition) {
        BuffSetting config = configs.get(id, false);
        if (config == null) {
            FormattingTuple message = MessageFormatter.format("标识为[{}]BUFF效果配置不存在", id);
            log.error(message.getMessage());
            throw new ManagedException(BattleResult.CONFIG_ERROR);
        }
        return initState(caster, target, config, buffReport, addition);
    }

    private BuffState initState(Unit caster, Unit target, BuffSetting config, BuffReport buffReport, Object addition) {
        /* 持续时间减免计算 */
        int keepTime = config.getTime();
        keepTime = keepTimeModifier(target, config, keepTime);
        return new BuffState(config, caster, buffReport, keepTime, addition);
    }

    private static int keepTimeModifier(Unit target, BuffSetting config, int keepTime) {
        if (config.getDispelType() == DispelType.HARMFUL) {
            keepTime = Math.max(1, (int) (keepTime * (1 - target.getRate(UnitRate.BUFF_HARM_DEC))));
        }
        return keepTime;
    }

    public static Formula getFormula(String formulaId) {
        return INSTANCE.formulaStorage.get(formulaId, false);
    }

    public static List<UnitState> dispelState(Unit target, DispelType dispelType, int time) {
        List<UnitState> removeStates = new ArrayList<>(5);
        for (UnitState unitState : UnitState.values()) {
            if (dispelType == DispelType.HARMFUL) {
                if (!unitState.harm) {
                    continue;
                }
            } else if (dispelType == DispelType.USEFUL) {
                if (unitState.harm) {
                    continue;
                }
            } else {
                continue;
            }
            if (target.hasState(unitState, time)) {
                target.removeState(unitState);
                removeStates.add(unitState);
            }
        }
        List<BuffState> harmBuff = target.getBuffByDispel(dispelType);
        if (harmBuff == null || harmBuff.size() == 0) {
            return removeStates;
        }
        for (BuffState buffState : harmBuff.toArray(new BuffState[0])) {
            BuffFactory.removeBuffState(buffState, target, time);
        }
        return removeStates;

    }

    public static void updateBuff(int time, Unit unit, int removeTimeParam, BuffState buffState, Object addition) {
        if (!isBuffUpdateSuccess(buffState, time, unit)) {
            return;
        }
        Action updateAction = new Action() {
            @Override
            public int getTime() {
                return time;
            }

            @Override
            public void execute() {
                if (unit.isDead()) {
                    return;
                }
                final int delay = removeTimeParam - time;
                int removeTime = time + keepTimeModifier(unit, buffState.getBuffSetting(), delay);
                buffState.updateRemoveTime(removeTime);
                final Buff buff = getBuff(buffState.getType());
                buff.update(buffState, unit, time, addition);
            }
        };
        unit.addTimedAction(updateAction);
    }

    private static boolean isBuffUpdateSuccess(BuffState buffState, int time, Unit owner) {
        if (buffState.getDispelType() == DispelType.HARMFUL) {
            return !owner.hasState(UnitState.WU_DI, time);
        }
        return true;
    }

    private static BuffState whoShouldBeOverwritten(BuffState pre, BuffState post) {
        return (pre.getBuffSetting().getLevel() - post.getBuffSetting().getLevel()) >= 0 ? pre : post;
    }

    public static void removeBuffsByDispelType(Unit owner, int time, DispelType dispelType) {
        List<BuffState> removingBuffs = owner.getBuffByDispel(dispelType);
        if (removingBuffs != null && removingBuffs.size() != 0) {
            for (BuffState buffState : removingBuffs.toArray(new BuffState[0])) {
                BuffFactory.removeBuffState(buffState, owner, time);
            }
        }
    }
}
