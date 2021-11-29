package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.action.Action;
import com.pangu.logic.module.battle.service.action.SkillEffectAction;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.select.utils.BestCircle;
import com.pangu.logic.module.battle.service.select.select.utils.Circle;
import com.pangu.logic.module.battle.service.select.select.utils.Rectangle;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.AreaParam;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import com.pangu.logic.module.battle.service.skill.param.ShenPanGangFengParam;
import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 审判罡风:
 * 朝目标发射两道弧形运动罡风,对路径上的敌军造成80%的物理攻击的伤害当罡风汇聚时,它们会合成一股巨型龙卷风,在2秒持续造成180%的物理攻击的伤害
 * 2级:对旋风移动路径上的敌人造成的伤害提升至100%
 * 3级:大龙卷造成伤害时,同时会将敌军吹起来
 * 4级:大龙卷伤害提升至220%
 */
@Component
public class ShenPanGangFeng implements SkillEffect {
    @Autowired
    private HpPhysicsDamage physicsDamage;
    @Autowired
    private StateAddEffect stateAddEffect;

    @Override
    public EffectType getType() {
        return EffectType.SHEN_PAN_GANG_FENG;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final ShenPanGangFengParam param = state.getParam(ShenPanGangFengParam.class);

        //  计算爆炸范围
        final List<Unit> enemies = FilterType.ENEMY.filter(owner, time);
        final List<Point> enemyPoints = enemies.stream().map(Unit::getPoint).collect(Collectors.toList());
        final int radius = param.getRadius();
        final Point ownerPoint = owner.getPoint();
        final Point bestCenter = BestCircle.calBestPoint(ownerPoint, enemyPoints, radius, BattleConstant.MAX_X, BattleConstant.MAX_Y, false);
        final Circle bestCircle = new Circle(bestCenter.x, bestCenter.y, radius);

        //  计算爆炸延时
        final int trackSpeed = skillState.getTrackSpeed();
        if (trackSpeed == 0) {
            return;
        }
        final int distance = ownerPoint.distance(bestCenter);
        final int explosionTime = SkillEffectAction.getFlyTime(distance, trackSpeed);
        final int explosionActTime = explosionTime + time;

        //  提交爆炸行为
        final Action explosionAct = new Action() {
            private int time = explosionActTime;
            private Circle circle = bestCircle;

            @Override
            public int getTime() {
                return time;
            }

            @Override
            public void execute() {
                Context context = new Context(owner);
                final EffectState effectState = new EffectState(state.getSetting(), 0);
                final StateAddParam stateParam = param.getStateAddParam();
                final DamageParam dmgParam = param.getCircleDmg();
                final ArrayList<Unit> enemiesInCircle = new ArrayList<>();
                for (Unit enemy : FilterType.ENEMY.filter(owner, time)) {
                    final Point point = enemy.getPoint();
                    if (!circle.inShape(point.x, point.y)) {
                        continue;
                    }
                    enemiesInCircle.add(enemy);
                }
                //伤害
                effectState.setParamOverride(dmgParam);
                for (Unit enemy : enemiesInCircle) {
                    context.execAttackBeforePassiveAndEffect(time, physicsDamage, enemy, skillState, effectState, skillReport);
                }
                //控制
                if (stateParam != null) {
                    effectState.setParamOverride(stateParam);
                    for (Unit enemy : enemiesInCircle) {
                        context.execAttackBeforePassiveAndEffect(time, stateAddEffect, enemy, skillState, effectState, skillReport);
                    }
                }
                context.execute(time, skillState, skillReport);
                skillReport.setAreaParam(AreaParam.from(circle), time);
            }
        };
        owner.addTimedAction(explosionAct);

        //  计算飞行路径
        final Rectangle rectangle = new Rectangle(ownerPoint, bestCenter, param.getWidth(), distance);
        //  计算飞行延时
        int totalDistanceOfEnemyInRect = 0;
        int enemyCountInRect = 0;
        for (Unit enemy : enemies) {
            final Point point = enemy.getPoint();
            if (!rectangle.inRect(point.x, point.y)) {
                continue;
            }
            totalDistanceOfEnemyInRect += ownerPoint.distance(point);
            enemyCountInRect++;
        }

        int roadDmgActTime = explosionActTime;
        if (enemyCountInRect > 0) {
            final int avgDist = totalDistanceOfEnemyInRect / enemyCountInRect;
            final int roadDmgFlyTime = SkillEffectAction.getFlyTime(avgDist, trackSpeed);
            roadDmgActTime  = roadDmgFlyTime + time;
        }
        final int finalRoadDmgActTime = roadDmgActTime;

        //  提交飞行行为
        final Action roadDmgAct = new Action() {
            private int time = finalRoadDmgActTime;
            private Rectangle rect = rectangle;

            @Override
            public int getTime() {
                return time;
            }

            @Override
            public void execute() {
                Context context = new Context(owner);
                final EffectState effectState = new EffectState(state.getSetting(), 0);
                final DamageParam dmgParam = param.getRoadDmg();
                effectState.setParamOverride(dmgParam);
                for (Unit enemy : FilterType.ENEMY.filter(owner, time)) {
                    final Point point = enemy.getPoint();
                    if (!rect.inRect(point.x, point.y)) {
                        continue;
                    }
                    context.execAttackBeforePassiveAndEffect(time, physicsDamage, enemy, skillState, effectState, skillReport);
                }
                context.execute(time, skillState, skillReport);
            }
        };
        owner.addTimedAction(roadDmgAct);
    }
}
