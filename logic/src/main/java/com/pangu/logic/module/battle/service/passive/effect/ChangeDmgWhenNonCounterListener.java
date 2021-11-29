package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitHpChangePassive;
import com.pangu.logic.module.battle.service.passive.param.ChangeDmgWhenNonCounterListenerParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 调整攻击者对非克制目标所造成的伤害
 */
@Component
public class ChangeDmgWhenNonCounterListener implements UnitHpChangePassive {
    @Override
    public PassiveType getType() {
        return PassiveType.CHANGE_DMG_WHEN_NON_COUNTER_LISTENER;
    }

    @Override
    public void hpChange(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> changeUnit) {
        final ChangeDmgWhenNonCounterListenerParam param = passiveState.getParam(ChangeDmgWhenNonCounterListenerParam.class);
        final boolean ownerListenFriend = param.isOwnerListenFriend();
        final boolean attackerIsOwnerFriend = owner.isFriend(attacker);

        if (!attacker.isJoinFighter()) {
            return;
        }

        if (ownerListenFriend && !attackerIsOwnerFriend || !ownerListenFriend && attackerIsOwnerFriend) {
            return;
        }

        for (Unit unit : changeUnit) {
            if (attacker.counter(unit)) {
                continue;
            }

            if (unit.isDead()) {
                continue;
            }

            //实际生命值变更
            final long actualHpChange = context.getActualHpChange(unit);
            if (actualHpChange >= 0) {
                continue;
            }

            //总伤害
            final long totalDmg = context.getTotalHpChange(unit);
            //基于总伤害的调整伤害
            final long adjustTotalDmg = (long) (totalDmg * (1 + param.getHpChangeRate()));
            //最终调整伤害
            final long finalDmg = -actualHpChange + adjustTotalDmg;

            if (finalDmg <= 0) {
                continue;
            }

            PassiveUtils.hpUpdate(context, damageReport, owner, unit, finalDmg, time, passiveState);
        }
    }
}
