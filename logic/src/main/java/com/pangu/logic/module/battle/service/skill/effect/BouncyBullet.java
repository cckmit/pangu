package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.action.Action;
import com.pangu.logic.module.battle.service.action.SkillEffectAction;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.BouncyBulletParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 远程形态时，每次普攻投掷的飞镰都可以在一定范围内的敌方之间弹跳。每次弹跳造成的伤害都会减少，最多弹射4次，每次伤害降低20%。
 */
@Component
public class BouncyBullet implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.BOUNCY_BULLET;
    }

    @Autowired
    private HpHigherDamage higherDamage;

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final BouncyBulletParam param = state.getParam(BouncyBulletParam.class);

        //  造成伤害
        state.setParamOverride(param.getDamageParam());
        higherDamage.execute(state, owner, target, skillReport, time, skillState, context);
        state.setParamOverride(null);

        final Set<Unit> attackedUnits = new HashSet<>();
        attackedUnits.add(target);

        //  计算下一个弹射目标
        final List<Unit> units = FilterType.ENEMY.filter(owner, time);
        Unit targetInRange = null;
        int distance = 0;
        for (Unit unit : units) {
            final int dist = unit.calcDistance(target);
            if (attackedUnits.contains(unit)) {
                continue;
            }
            if (dist < param.getBounceRange()) {
                targetInRange = unit;
                distance = dist;
                break;
            }
        }
        if (targetInRange == null) {
            return;
        }
        final Unit nextTarget = targetInRange;

        //  计算当前目标到弹射至下一个目标所需时间（默认下一个目标不会移动，移动也没办法了）
        final int flyTime = SkillEffectAction.getFlyTime(distance, skillState.getTrackSpeed());

        Action action = new Action() {
            /**
             * 已弹射次数。弹射距离，速度和伤害会随弹射次数增加而递减
             */
            private int bouncedCount = 1;

            /**
             * 执行时间
             */
            private int execTime = flyTime + time;

            /**
             * 下一个攻击目标
             */
            private Unit target = nextTarget;

            /**
             * 已命中的目标不再弹射
             */
            private Set<Unit> attackedTarget = attackedUnits;

            @Override
            public int getTime() {
                return execTime;
            }

            @Override
            public void execute() {
                //  伤害衰减
                final EffectState effectState = new EffectState(null, 0);
                final double factor = Math.max(param.getMinFactor(), param.getDamageParam().getFactor() * (1 - bouncedCount * param.getDmgDecr()));
                effectState.setParamOverride(param.getDamageParam().copy(factor));
                context.execAttackBeforePassiveAndEffect(execTime, higherDamage, target, skillState, effectState, skillReport);
                attackedTarget.add(target);

                //  准备下一次弹射
                final List<Unit> units = FilterType.FRIEND_WITHOUT_SELF.filter(target, execTime);
                Unit targetInRange = null;
                int distance = 0;
                final int nextBoundRange = (int) Math.max(param.getMinRange(), param.getBounceRange() * (1 - bouncedCount * param.getRangeDecr()));
                for (Unit unit : units) {
                    if (attackedUnits.contains(unit)) {
                        continue;
                    }
                    final int dist = unit.calcDistance(target);
                    if (dist < nextBoundRange) {
                        targetInRange = unit;
                        distance = dist;
                        break;
                    }
                }
                if (targetInRange == null) {
                    return;
                }

                //  计算当前目标到弹射至下一个目标所需时间（默认下一个目标不会移动，移动也没办法了）
                int nextSpd = (int) Math.max(param.getMinSpd(), (skillState.getTrackSpeed() * (1 - bouncedCount * param.getSpdDecr())));
                execTime += SkillEffectAction.getFlyTime(distance, nextSpd);
                owner.addTimedAction(this);

                attackedTarget.add(target);
                bouncedCount = bouncedCount + 1;
                target = targetInRange;
            }
        };
        owner.addTimedAction(action);
    }
}
