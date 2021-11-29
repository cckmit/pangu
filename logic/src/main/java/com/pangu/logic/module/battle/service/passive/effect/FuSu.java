package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitHpChangePassive;
import com.pangu.logic.module.battle.service.passive.param.FuSuParam;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 复苏（血量第一次低于X%时，恢复Y%的血量）
 *
 * @author Kubby
 */
@Component
public class FuSu implements UnitHpChangePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.FU_SU;
    }

    @Override
    public void hpChange(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context,
                         ITimedDamageReport damageReport, Set<Unit> changeUnit) {
        if (owner.isDead() || owner.getValue(UnitValue.HP) <= 0) {
            return;
        }

        boolean trigger = passiveState.getAddition(boolean.class, false);
        if (trigger) {
            return;
        }

        FuSuParam param = passiveState.getParam(FuSuParam.class);
        if (owner.getHpPct() >= param.getHpPct()) {
            return;
        }

        passiveState.setAddition(true);

        long cureHp = (long) (owner.getValue(UnitValue.HP_MAX) * param.getCurePct());
        context.passiveRecover(owner, owner, cureHp, time, passiveState, damageReport);
    }

}
