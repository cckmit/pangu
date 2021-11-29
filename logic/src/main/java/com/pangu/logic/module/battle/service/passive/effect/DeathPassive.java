package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Death;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackEndPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import org.springframework.stereotype.Component;

/**
 * 死神雕像 受到死神青睐 收割攻击后生命值低于15%的单元
 */
@Component
public class DeathPassive implements AttackEndPassive {
    @Override
    public PassiveType getType() {
        return PassiveType.DEATH;
    }

    @Override
    public void attackEnd(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final Double rate = passiveState.getParam(Double.class);
        long realHp = target.getValue(UnitValue.HP) + context.getHpChange(target);
        final long hp_max = target.getValue(UnitValue.HP_MAX);
        double hpRate = realHp / 1D / hp_max;
        if (hpRate <= rate) {
            target.foreverDead();
            skillReport.add(time, target.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), new Death()));
        }
    }
}
