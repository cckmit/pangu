package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.LeiTingShenFaParam;
import org.springframework.stereotype.Component;

/**
 * 雷霆神罚
 * 对范围内全体目标造成7次打击,共造成230%法攻伤害范围内的目标越少则每个目标承受的伤害越高最高可加成15%伤害,对拥有护盾的目标可造成3倍伤害
 * 2级:技能伤害提升至260%
 * 3级:范围内的目标越少每个目标承受的伤害越高增加,最高可加成25%伤害
 * 4级:技能伤害提升至290%
 */
@Component
public class LeiTingShenFa implements AttackPassive {
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (damage >= 0) return;
        if (skillState.getType() != SkillType.SPACE) return;
        final LeiTingShenFaParam param = passiveState.getParam(LeiTingShenFaParam.class);
        long deepen;
        if (param.getFactorForShieldTarget() > 0 && target.getValue(UnitValue.SHIELD) > 0) {
            deepen = (long) (damage * (param.getFactorForShieldTarget() - 1d));
        } else {
            final double ratePerEnemey = param.getRatePerEnemy();
            final int baseCount = param.getBaseCount();
            final int targetAmount = context.getTargetAmount();
            if (param.isNegative()) {
                final double addRate = Math.min(Math.max(0, (baseCount - targetAmount)) * ratePerEnemey, param.getMaxRate());
                deepen = (long) (addRate * damage);
            } else {
                final double addRate = Math.min(Math.max(0, (targetAmount - baseCount)) * ratePerEnemey, param.getMaxRate());
                deepen = (long) (addRate * damage);
            }
        }
        context.addPassiveValue(target, AlterType.HP, deepen);
        skillReport.add(time, target.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), Hp.of(deepen)));
    }

    @Override
    public PassiveType getType() {
        return PassiveType.LEI_TING_SHEN_FA;
    }
}
