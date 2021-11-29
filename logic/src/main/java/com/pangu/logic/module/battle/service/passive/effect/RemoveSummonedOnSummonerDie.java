package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.values.Death;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.OwnerDiePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import org.springframework.stereotype.Component;

/**
 * 召唤者死亡时移除其所有召唤物
 */
@Component
public class RemoveSummonedOnSummonerDie implements OwnerDiePassive {
    @Override
    public PassiveType getType() {
        return PassiveType.REMOVE_SUMMONED_ON_SUMMONER_DIE;
    }

    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attack, ITimedDamageReport timedDamageReport, int time, Context context) {
        if (!owner.isDead()) {
            return;
        }
        final Object addition = passiveState.getAddition();
        final Unit[] units = addition instanceof Unit[] ? ((Unit[]) addition) : null;
        if (units != null) {
            for (Unit unit : units) {
                unit.dead();
                timedDamageReport.add(time, unit.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), new Death()));
            }
        } else {
            for (Unit unit : owner.getBattle().getActionUnits()) {
                if (unit.getSummonUnit() != owner) {
                    continue;
                }
                unit.dead();
                timedDamageReport.add(time, unit.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), new Death()));
            }
        }
    }
}
