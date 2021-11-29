package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import org.springframework.stereotype.Component;

/**
 * 普通攻击每命中一个处于异常状态中的目标，会使自己增加10%的暴击率，直到有攻击暴击之后会重新计算
 */
@Component
public class BaoLie implements AttackPassive {
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        //  暴击时重置暴击率
        if (context.isCrit(target)) {
            final Double accCritRate = passiveState.getAddition(double.class, 0d);
            if (accCritRate != 0) {
                passiveState.setAddition(0d);
                context.addPassiveValue(owner, AlterType.RATE_CRIT, -accCritRate);
                skillReport.add(time, owner.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), new UnitValues(AlterType.RATE_CRIT, -accCritRate)));
            }
            return;
        }

        //  非普攻不累加暴击率
        if (skillState.getType() != SkillType.NORMAL) {
            return;
        }
        //  目标未处于有害异常状态不触发
        if (!target.inHarmfulState(time)) {
            return;
        }

        final double critAdd = passiveState.getParam(double.class);
        context.addPassiveValue(owner, AlterType.RATE_CRIT, critAdd);
        passiveState.setAddition(passiveState.getAddition(double.class, 0d) + critAdd);
        skillReport.add(time, owner.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), new UnitValues(AlterType.RATE_CRIT, critAdd)));
    }

    @Override
    public PassiveType getType() {
        return PassiveType.BAO_LIE;
    }
}
