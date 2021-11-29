package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.model.report.values.PositionChange;
import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.MoveWhenDmgParam;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.select.select.utils.TwoPointDistanceUtils;
import com.pangu.logic.module.battle.service.skill.ctx.OwnerTargetCtx;
import com.pangu.logic.utils.ExpressionHelper;
import com.pangu.framework.utils.reflect.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * 受到（坦克，战士与刺客职业）攻击时，把敌人传送至友方前排英雄身边，每5秒触发1次
 */
@Component
public class MoveWhenDmg implements DamagePassive {

    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final MoveWhenDmgParam param = passiveState.getParam(MoveWhenDmgParam.class);

        //  触发校验
        final String condition = param.getTriggerExp();
        if (!StringUtils.isEmpty(condition)
                && !ExpressionHelper.invoke(condition, boolean.class, new OwnerTargetCtx(time, owner, attacker, 0, context))) {
            return;
        }

        //  计算目的地
        Point destPoint = null;
        final Point ownerPoint = owner.getPoint();
        switch (param.getDestType()) {
            case TARGET: {
                final String targetPointId = param.getTargetPoint();
                final List<Unit> targetPoints = TargetSelector.select(owner, targetPointId, time);
                if (CollectionUtils.isEmpty(targetPoints)) {
                    return;
                }


                Point targetPoint = null;
                for (Unit unit : targetPoints) {
                    if (unit != owner) {
                        targetPoint = unit.getPoint();
                        break;
                    }
                }
                if (targetPoint == null) {
                    targetPoint = ownerPoint;
                }
                destPoint = TwoPointDistanceUtils.getNearEndPointDistance(ownerPoint, targetPoint, BattleConstant.SCOPE);
                break;
            }
            case EMPTY_SELF_CORNER: {
                //  根据阵营筛选出我方角落
                final Point[] cornerPoints = new Point[2];
                if (owner.getFriend().isAttacker()) {
                    cornerPoints[0] = new Point(0, 0);
                    cornerPoints[1] = new Point(0, BattleConstant.MAX_Y);
                } else {
                    cornerPoints[0] = new Point(BattleConstant.MAX_X, 0);
                    cornerPoints[1] = new Point(BattleConstant.MAX_X, BattleConstant.MAX_Y);
                }

                //  筛选出人最少的角落
                Point emptyCorner = null;
                int minCountInCorner = Integer.MAX_VALUE;
                final List<Unit> allUnits = FilterType.ALL.filter(owner, time);
                for (Point cornerPoint : cornerPoints) {
                    int countInCorner = 0;
                    for (Unit unit : allUnits) {
                        if (cornerPoint.distance(unit.getPoint()) < BattleConstant.SCOPE * 3) {
                            countInCorner++;
                        }
                    }
                    if (countInCorner < minCountInCorner) {
                        minCountInCorner = countInCorner;
                        emptyCorner = cornerPoint;
                    }
                }

                destPoint = TwoPointDistanceUtils.getNearStartPoint(emptyCorner, BattleConstant.SCOPE_HALF, ownerPoint);
                break;
            }
            default:
                return;
        }


        //  计算需要移动的目标
        final String targetToMove = param.getTargetToMove();
        List<Unit> targets;
        if (StringUtils.isEmpty(targetToMove)) {
            targets = Collections.singletonList(attacker);
        } else {
            targets = TargetSelector.select(owner, targetToMove, time);
        }

        final String passiveId = passiveState.getId();
        final String ownerId = owner.getId();
        for (Unit target : targets) {
            target.move(destPoint);
            final Point point = target.getPoint();
            skillReport.add(time, target.getId(), PassiveValue.single(passiveId, ownerId, PositionChange.of(point.x, point.y)));
        }

        passiveState.addCD(time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.MOVE_WHEN_DMG;
    }
}
