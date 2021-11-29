package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitHpChangePassive;
import com.pangu.logic.module.battle.service.passive.param.BuffCastOnHpDownParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 当生命值低于40%时进入防御状态，站在原地无法移动，防御力提升50%，普通攻击范围增加，每3秒恢复1%最大生命值，当前生命值超过75%时，解除防御状态
 */
@Component
public class BuffCastOnHpDown implements UnitHpChangePassive {
    @Override
    public PassiveType getType() {
        return PassiveType.BUFF_CAST_ON_HP_DOWN;
    }

    @Override
    public void hpChange(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> changeUnit) {
        if (!changeUnit.contains(owner)) {
            return;
        }
        final BuffCastOnHpDownParam param = passiveState.getParam(BuffCastOnHpDownParam.class);
        if (owner.getHpPct() > param.getTriggerHpPct()) {
            return;
        }
        final String buff = param.getBuff();
        final List<BuffState> states = owner.getBuffBySettingId(buff);
        if (!states.isEmpty()) {
            return;
        }
        BuffFactory.addBuff(buff, owner, owner, time, damageReport, null);
    }
}
