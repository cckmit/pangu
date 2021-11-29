package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import org.springframework.stereotype.Component;

/**
 * 攻击链 传递当前攻击伤害的15%给其他所有敌方英雄
 */
@Component
public class AttackChain implements AttackPassive {
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final Double param = passiveState.getParam(Double.class);
        long increaseDamage = (long) (param * damage);
        for (Unit unit : target.getFriend().getCurrent()) {
            if (unit == target || !unit.canSelect(time)) {
                continue;
            }
            context.addPassiveValue(unit, AlterType.HP, increaseDamage);
            skillReport.add(time, unit.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), Hp.of(increaseDamage)));
        }
    }

    @Override
    public PassiveType getType() {
        return PassiveType.ATTACK_CHAIN;
    }
}
