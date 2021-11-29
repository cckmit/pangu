package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.values.Mp;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitDiePassive;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 召唤物击杀单元后 给召唤者回蓝
 */
@Component
public class SummonMp implements UnitDiePassive {
    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> dieUnits) {
        if (owner != attacker) {
            return;
        }
        final Unit unit = owner.getSummonUnit();
        if (unit == null || unit.isDead()) {
            return;
        }
        final Integer param = passiveState.getParam(Integer.class);
        int addMp = dieUnits.size() * param;
        context.addPassiveValue(unit, AlterType.MP, addMp);
        damageReport.add(time, unit.getId(), new Mp(addMp));
        passiveState.addCD(time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.SUMMON_MP;
    }
}
