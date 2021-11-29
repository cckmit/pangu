package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitHpChangePassive;
import com.pangu.logic.module.battle.service.passive.param.CureOnOwnerHpDownParam;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.effect.HpRecover;
import com.pangu.logic.module.battle.service.skill.param.HpRecoverParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 奥米茄的英勇使他可以重振队伍士气，当自身生命值首次低于20%时,使场上的所有友军回复400%攻击力的生命。
 */
@Component
public class CureOnOwnerHpDown implements UnitHpChangePassive {
    @Override
    public PassiveType getType() {
        return PassiveType.CURE_ON_OWNER_HP_DOWN;
    }

    @Autowired
    private HpRecover recover;

    @Override
    public void hpChange(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> changeUnit) {
        if (!changeUnit.contains(owner)) {
            return;
        }
        final CureOnOwnerHpDownParam param = passiveState.getParam(CureOnOwnerHpDownParam.class);
        if (param.getTriggerHpPct() < owner.getHpPct()) {
            return;
        }

        final List<Unit> select = TargetSelector.select(owner, param.getCureTarget(), time);
        final HpRecoverParam recoverParam = param.getRecoverParam();
        for (Unit unit : select) {
            final HpRecover.RecoverResult res = recover.calcRecoverRes(owner, unit, recoverParam);
            context.passiveRecover(owner, unit, res.getRecover(), time, passiveState, damageReport);
            BuffFactory.addBuff(param.getBuff(), owner, unit, time, damageReport, null);
        }

        passiveState.addCD(time);
    }
}
