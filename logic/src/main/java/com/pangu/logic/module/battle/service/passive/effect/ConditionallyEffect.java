package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.*;
import com.pangu.logic.module.battle.service.passive.param.ConditionallyEffectParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.effect.HpHigherDamage;
import com.pangu.logic.module.battle.service.skill.effect.HpRecover;
import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import com.pangu.framework.utils.reflect.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 通用的基于条件表达式的属性修改器+buff添加器+伤害造成器
 */
@Component
public class ConditionallyEffect implements AttackPassive, DamagePassive {
    @Autowired
    private HpHigherDamage higherDamage;
    @Autowired
    private HpRecover hpRecover;

    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final ConditionallyEffectParam param = getParam(passiveState);
        if (param.getPhase() != Phase.ATTACK) {
            return;
        }
        verifyAndDoSth(passiveState, owner, target, skillState, damage, context, param, time, skillReport);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.CONDITIONALLY_EFFECT;
    }

    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final ConditionallyEffectParam param = getParam(passiveState);
        if (param.getPhase() != Phase.DAMAGE) {
            return;
        }
        verifyAndDoSth(passiveState, owner, attacker, skillState, context.getHpChange(owner), context, param, time, skillReport);
    }

    private void verifyAndDoSth(PassiveState passiveState, Unit owner, Unit target, SkillState skillState, long damage, Context context, ConditionallyEffectParam param, int time, SkillReport skillReport) {
        if (!atkPassiveVerify(passiveState, owner, target, damage, time, context, skillState, param.getConExp())) {
            return;
        }

        final List<Unit> targets = getTargets(owner, target, time, param.getTargetId());
        final String passiveId = passiveState.getId();
        final Unit caster = passiveState.getCaster();
        final long dmgChange = (long) (param.getDmgCorrFactor() * damage);
        for (Unit unit : targets) {
            //修改数值
            context.modVal(owner, unit, time, skillReport, param.getValModParam(), passiveId, caster);
            //造成伤害
            context.passiveAtkDmg(owner, unit, time, skillReport, higherDamage, param.getDmgParam(), passiveId, caster);
            //治疗效果
            context.passiveRecover(owner, target, time, hpRecover, param.getRecoverParam(), passiveState, skillReport, passiveState.getCaster());
            //添加BUFF
            BuffFactory.addBuff(param.getBuffId(), owner, unit, time, skillReport, null);
            //伤害修正
            if (dmgChange != 0) {
                PassiveUtils.hpUpdate(context, skillReport, owner, unit, dmgChange, time, passiveState);
            }
        }

        final StateAddParam stateParam = param.getStateParam();
        if (stateParam == null) {
            return;
        }
        //添加异常状态
        for (Unit unit : targets) {
            PassiveUtils.addState(owner, unit, stateParam.getState(), time + stateParam.getTime(), time, passiveState, context, skillReport);
        }
    }

    private List<Unit> getTargets(Unit owner, Unit target, int time, String targetId) {
        if (!StringUtils.isEmpty(targetId)) {
            return TargetSelector.select(owner, targetId, time);
        } else {
            return Collections.singletonList(target);
        }
    }

    private ConditionallyEffectParam getParam(PassiveState state) {
        return state.getParam(ConditionallyEffectParam.class);
    }
}
