package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.DecreaseOverflowDamageParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import org.springframework.stereotype.Component;

/**
 * 超过6%最大生命值的伤害 降低75%
 */
@Component
public class DecreaseOverflowDamage implements DamagePassive {
    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final long change = context.getHpChange(owner);
        if (change >= 0) {
            return;
        }
        final DecreaseOverflowDamageParam param = passiveState.getParam(DecreaseOverflowDamageParam.class);
        final double hpRate = param.getHpRate();
        final long hpMax = owner.getValue(UnitValue.HP_MAX);
        final long overflowDamage = (long) (hpRate * hpMax + change);
        if (overflowDamage >= 0) {
            return;
        }
        long increaseHp = (long) (-overflowDamage * param.getDecreaseRate());

        PassiveUtils.hpUpdate(context, skillReport, owner, increaseHp, time);

        passiveState.addCD(time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.DECREASE_OVERFLOW_DAMAGE;
    }
}
