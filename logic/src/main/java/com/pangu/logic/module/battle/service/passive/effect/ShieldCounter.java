package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import org.springframework.stereotype.Component;

/**
 * 对持有护盾的目标造成额外伤害
 */
@Component
public class ShieldCounter implements AttackPassive {
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (target.getValue(UnitValue.SHIELD) <= 0) {
            return;
        }

        final long dmgChange = (long) (damage * passiveState.getParam(Double.class));
        PassiveUtils.hpUpdate(context, skillReport, target, dmgChange, time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.SHIELD_COUNTER;
    }
}
