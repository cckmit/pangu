package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.DecreaseDamageFromSequenceParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 受到来自前排的伤害降低10%
 */
@Component
public class DecreaseDamageFromSequence implements DamagePassive {
    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final DecreaseDamageFromSequenceParam param = passiveState.getParam(DecreaseDamageFromSequenceParam.class);
        final List<Integer> sequences = param.getSequences();
        if (sequences == null) {
            return;
        }
        if (!sequences.contains(attacker.getSequence())) {
            return;
        }
        long increaseHp = (long) (-damage * param.getRate());

        PassiveUtils.hpUpdate(context, skillReport, owner, increaseHp, time);

        passiveState.addCD(time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.DECREASE_DAMAGE_FROM_SEQUENCE;
    }
}
