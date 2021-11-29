package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.values.Mp;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.OwnerDiePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import org.springframework.stereotype.Component;

import java.util.List;

//死亡后平分MP给队友
@Component
public class DivideMpAfterDeath implements OwnerDiePassive {
    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attack, ITimedDamageReport timedDamageReport, int time, Context context) {
        final List<Unit> current = owner.getFriend().getCurrent();
        final long value = owner.getValue(UnitValue.MP) / current.size();
        if (value <= 0) {
            return;
        }
        for (Unit unit : current) {
            timedDamageReport.add(time, unit.getId(), new Mp(value));
            context.addPassiveValue(unit, AlterType.MP, value);
        }
        passiveState.addCD(time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.DIVIDE_MP_AFTER_DEATH;
    }
}
