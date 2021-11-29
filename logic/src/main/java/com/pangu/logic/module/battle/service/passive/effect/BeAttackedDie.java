package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Death;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import org.springframework.stereotype.Component;

/**
 * 被攻击几次后死亡
 */
@Component
public class BeAttackedDie implements DamagePassive {

    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final Integer param = passiveState.getParam(Integer.class);
        Integer times = passiveState.getAddition(Integer.class, 0);
        if (times >= param) {
            owner.foreverDead();
            skillReport.add(time, owner.getId(), new Death());
        }
        passiveState.setAddition(++times);
        passiveState.addCD(time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.BE_ATTACK_DIE;
    }
}
