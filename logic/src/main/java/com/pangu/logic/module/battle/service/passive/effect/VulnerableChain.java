package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 持有此被动的单位受到伤害时，其队友也会受到连累
 */
@Component
public class VulnerableChain implements DamagePassive {

    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final Double param = passiveState.getParam(Double.class);
        long increaseDamage = (long) (param * damage);
        final List<Unit> friends = FilterType.FRIEND.filter(owner, time);
        for (Unit unit : friends) {
            if (unit == owner) {
                continue;
            }
            context.addPassiveValue(unit, AlterType.HP, increaseDamage);
            skillReport.add(time, unit.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), Hp.of(increaseDamage)));
        }
    }

    @Override
    public PassiveType getType() {
        return PassiveType.VULNERABLE_CHAIN;
    }
}
