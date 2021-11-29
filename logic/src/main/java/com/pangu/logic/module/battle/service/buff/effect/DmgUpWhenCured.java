package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitHpChangePassive;
import com.pangu.logic.module.battle.service.passive.param.DmgUpWhenCuredParam;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 每累计受到10%的治疗时，提升1%的伤害
 */
@Component
public class DmgUpWhenCured implements UnitHpChangePassive {
    @Override
    public PassiveType getType() {
        return PassiveType.DMG_UP_WHEN_CURED;
    }

    @Override
    public void hpChange(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> changeUnit) {
        if (!changeUnit.contains(owner)) {
            return;
        }

        final Addition addition = passiveState.getAddition(Addition.class, new Addition());
        Long preHp = addition.preHp;
        final long hpMax = owner.getValue(UnitValue.HP_MAX);
        if (preHp == null) {
            preHp = hpMax;
        }
        final long curHp = owner.getValue(UnitValue.HP);
        addition.preHp = curHp;

        final long hpChange = curHp - preHp;
        if (hpChange <= 0) {
            return;
        }

        final double recoverPct = hpChange / 1.0 / hpMax;
        final DmgUpWhenCuredParam param = passiveState.getParam(DmgUpWhenCuredParam.class);
        final double curedHpPct = param.getCuredHpPct();
        final Number triggerTimes = (addition.remainder + recoverPct) / curedHpPct;
        final long lTriggerTimes = triggerTimes.longValue();
        addition.remainder = (triggerTimes.doubleValue() - lTriggerTimes) * curedHpPct;
        if (lTriggerTimes <= 0) {
            return;
        }
        final double totalDmgRate = lTriggerTimes * param.getDmgUpRate();
        context.addPassiveValue(owner, AlterType.RATE_HARM_M,totalDmgRate);
        context.addPassiveValue(owner, AlterType.RATE_HARM_P,totalDmgRate);
    }

    private static class Addition {
        private Long preHp;
        private double remainder;
    }
}
