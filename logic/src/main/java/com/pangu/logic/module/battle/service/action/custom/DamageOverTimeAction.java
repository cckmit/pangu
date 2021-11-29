package com.pangu.logic.module.battle.service.action.custom;

import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Miss;
import com.pangu.logic.module.battle.service.action.Action;
import com.pangu.logic.module.battle.service.core.*;
import com.pangu.logic.module.battle.service.skill.CommonFormula;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import com.pangu.logic.module.battle.service.skill.param.DamageOverTimeParam;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import lombok.Getter;

import java.util.List;


/**
 * 持续性伤害行为
 */
@Getter
public class DamageOverTimeAction implements Action {
    private int time;

    private final Unit owner;

    private final SkillState skillState;

    private final SkillReport skillReport;

    private final EffectState effectState;

    private final List<Unit> targets;

    private int loops = 1;

    public DamageOverTimeAction(int time, Unit owner, SkillState skillState, SkillReport skillReport, EffectState effectState, List<Unit> targets) {
        this.time = time;
        this.owner = owner;
        this.skillState = skillState;
        this.skillReport = skillReport;
        this.effectState = effectState;
        this.targets = targets;
    }

    @Override
    public void execute() {
        final DamageOverTimeParam param = effectState.getParam(DamageOverTimeParam.class);
        //循环次数超过约定次数，结束DOT
        if(param.getLoops() < loops)return;

        final SkillEffect dmgEffect = SkillFactory.getSkillEffect(param.getEffectType());
        // 对每个施放目标做独立运算
        Context context = new Context(owner);
        Fighter friend = owner.getFriend();
        for (Unit target : targets) {
            if (target.isDead()) {
                continue;
            }
            context.executeAttackBeforePassiveStart(time, target, effectState, skillReport);
            context.executeBeAttackBeforePassiveStart(time, target, effectState, skillReport);
            // 队友不计算闪避
            if (target.getFriend() != friend) {
                // 判断是否命中
                boolean hit = CommonFormula.isHit(time, owner, target);
                if (!hit) {
                    skillReport.add(time, target.getId(), new Miss());
                    context.executeAttackBeforePassiveEnd(time, target, effectState, skillReport);
                    context.executeBeAttackBeforePassiveEnd(time, target, effectState, skillReport);
                    continue;
                }
            }
            effectState.setParamOverride(new DamageParam(param.getFactor()));
            dmgEffect.execute(effectState, owner, target, skillReport, time, skillState, context);
            effectState.setParamOverride(null);
            context.executeAttackBeforePassiveEnd(time, target, effectState, skillReport);
            context.executeBeAttackBeforePassiveEnd(time, target, effectState, skillReport);
        }
        context.execute(time, skillState, skillReport);

        //控制下次执行
        loops++;
        time += param.getInterval();
        if (skillState.getSetting().isIgnoreDie()) {
            owner.getBattle().addWorldAction(this);
        } else {
            owner.addTimedAction(this);
        }
    }
}
