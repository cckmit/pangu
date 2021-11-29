package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackBeforePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import org.springframework.stereotype.Component;

/**
 * 无视百分比防御
 */
@Component
public class IgnoreDefence implements AttackBeforePassive {
    @Override
    public void attackBefore(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time, Context context, SkillReport skillReport) {
        final Double rate = passiveState.getParam(Double.class);
        final IgnoreDefenceAddition addition = passiveState.getAddition(IgnoreDefenceAddition.class, new IgnoreDefenceAddition());
        final long decreaseDM = (long) (target.getValue(UnitValue.DEFENCE_M) * rate);
        final long decreaseDP = (long) (target.getValue(UnitValue.DEFENCE_P) * rate);
        target.increaseValue(UnitValue.DEFENCE_M, -decreaseDM);
        target.increaseValue(UnitValue.DEFENCE_P, -decreaseDP);

        addition.decreaseDefenceM = decreaseDM;
        addition.decreaseDefenceP = decreaseDP;
    }

    @Override
    public void attackEnd(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time, Context context, SkillReport skillReport) {
        final IgnoreDefenceAddition addition = passiveState.getAddition(IgnoreDefenceAddition.class, new IgnoreDefenceAddition());

        target.increaseValue(UnitValue.DEFENCE_M, addition.decreaseDefenceM);
        target.increaseValue(UnitValue.DEFENCE_P, addition.decreaseDefenceP);

        addition.decreaseDefenceP = 0;
        addition.decreaseDefenceM = 0;
        passiveState.addCD(time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.IGNORE_DEFENCE;
    }

    public static class IgnoreDefenceAddition {
        long decreaseDefenceM;

        long decreaseDefenceP;
    }
}
