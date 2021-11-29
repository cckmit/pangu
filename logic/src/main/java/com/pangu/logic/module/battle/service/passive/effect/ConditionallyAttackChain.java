package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.ConditionallyAttackChainParam;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * 奥古斯丁的暗影狂暴将会是目标一定范围内的人也受到伤害，伤害为50%
 */
@Component
public class ConditionallyAttackChain implements AttackPassive {
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final ConditionallyAttackChainParam param = passiveState.getParam(ConditionallyAttackChainParam.class);
        if (!atkPassiveVerify(passiveState, owner, target, damage, time, context, skillState, param.getTriggerCond())) {
            return;
        }
        final List<Unit> selects = TargetSelector.select(target, param.getTarget(), time);
        for (Unit select : selects) {
            final long dmg = (long) (damage * param.getRate());
            context.addPassiveValue(select, AlterType.HP, dmg);
            skillReport.add(time, select.getId(), Hp.of(dmg));
        }
    }

    @Override
    public PassiveType getType() {
        return PassiveType.CONDITIONALLY_ATTACK_CHAIN;
    }
}
