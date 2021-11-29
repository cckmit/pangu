package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.model.report.values.PositionChange;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveFactory;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.param.OwnerEffectByTagSkillReleaseParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.effect.HpHigherDamage;
import com.pangu.logic.module.battle.service.skill.effect.Repel;
import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * 被动持有者释放特定tag的技能时，执行某些行为
 */
@Component
public class OwnerEffectWhenSkillRelease implements SkillReleasePassive {
    @Autowired
    private HpHigherDamage higherDamage;
    @Autowired
    private Repel repel;

    @Override
    public PassiveType getType() {
        return PassiveType.OWNER_EFFECT_BY_TAG_SKILL_RELEASE;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time, Context context, SkillReport skillReport) {
        /*  非自身释放技能，不执行*/
        if (owner != attacker) {
            return;
        }

        final OwnerEffectByTagSkillReleaseParam param = passiveState.getParam(OwnerEffectByTagSkillReleaseParam.class);

        /*  非特定tag的技能，不执行*/
        if (!skillState.getTag().equals(param.getSkillTag())) {
            return;
        }

        /*  未通过概率校验，不执行*/
        if (param.getRate() > 0 && !RandomUtils.isHit(param.getRate())) {
            return;
        }

        /*  计算影响目标*/
        final List<Unit> targets = targetSelect(param.getTargetId(), skillState, owner, time);
        final String psvId = passiveState.getId();
        for (Unit target : targets) {
            context.passiveAtkDmg(owner, target, time, skillReport, higherDamage, param.getDmgParam(), psvId, null);
            context.modVal(owner, target, time, skillReport, param.getValModParam(), psvId, null);
            BuffFactory.addBuff(param.getBuffId(), owner, target, time, skillReport, null);
            repel(owner, target, param.getRepelDist(), passiveState.getId(), skillReport, time);

            final StateAddParam stateAddParam = param.getStateAddParam();
            if (stateAddParam != null) {
                PassiveUtils.addState(owner, target, stateAddParam.getState(), stateAddParam.getTime() + time, time, passiveState, context, skillReport);
            }

            if (param.getPassive() != null) {
                final PassiveState psvState = PassiveFactory.initState(param.getPassive(), time);
                owner.addPassive(psvState, owner);
            }
        }
    }

    private void repel(Unit owner, Unit target, int distance, String passiveId, SkillReport skillReport, int time) {
        if (distance == 0) {
            return;
        }
        final PositionChange positionReport = new PositionChange();
        final boolean succeed = repel.doRepel(distance, owner, target, time, positionReport);
        if (succeed) {
            skillReport.add(time, target.getId(), PassiveValue.single(passiveId, owner.getId(), positionReport));
        }
    }

    private List<Unit> targetSelect(String targetId, SkillState skillState, Unit owner, int time) {
        if (StringUtils.isEmpty(targetId)) {
            if (skillState == null) {
                return Collections.emptyList();
            }
            final List<EffectState> effectStates = skillState.getEffectStates();
            if (CollectionUtils.isEmpty(effectStates)) {
                return Collections.emptyList();
            }
            targetId = effectStates.get(0).getTarget();
        }

        if ("SELF".equals(targetId)) {
            return Collections.singletonList(owner);
        }

        return TargetSelector.select(owner, targetId, time);
    }
}
