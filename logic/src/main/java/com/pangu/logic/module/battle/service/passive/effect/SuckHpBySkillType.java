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
import com.pangu.logic.module.battle.service.passive.param.SuckHpBySkillTypeParam;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 细粒度吸血。可配置触发吸血的技能类型和回复生命的目标
 */
@Component
public class SuckHpBySkillType implements AttackPassive {
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final SuckHpBySkillTypeParam param = passiveState.getParam(SuckHpBySkillTypeParam.class);
        //攻击类型与配置不符，不执行吸血
        if (Arrays.stream(param.getTypes()).noneMatch(type -> skillState.getType() == type)) return;

        double rate = param.getRate();
        if (rate <= 0) {
            return;
        }

        // 计算回复总量
        long value = (long) (-context.getHpChange(target) * rate);

        // 筛选需要被回复的目标
        final List<Unit> recoverTargets = TargetSelector.select(owner, param.getTargetId(), time);
        for (Unit recoverTarget : recoverTargets) {
            final long recoverValue = value / recoverTargets.size();
            //  执行回复
            context.addPassiveValue(recoverTarget, AlterType.HP, recoverValue);
            //  生成战报
            final PassiveValue pv = PassiveValue.single(passiveState.getId(), owner.getId(), Hp.fromRecover(recoverValue));
            skillReport.add(time, recoverTarget.getId(), pv);
        }

        passiveState.addCD(time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.SUCK_HP_BY_SKILL_TYPE;
    }
}
