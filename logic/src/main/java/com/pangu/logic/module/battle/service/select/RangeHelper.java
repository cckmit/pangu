package com.pangu.logic.module.battle.service.select;

import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.List;

/**
 * 射程帮助类
 */
public final class RangeHelper {

    /**
     * 两点间距离
     *
     * @param a
     * @param b
     * @return
     */
    public static int getDistance(Point a, Point b) {
        return a.distance(b);
    }

    /**
     * 两个战斗单位间距离
     *
     * @param owner
     * @param target
     * @return
     */
    public static int getDistance(Unit owner, Unit target) {
        return getDistance(owner.getPoint(), target.getPoint());
    }

    /**
     * 判断目标是否在射程内
     *
     * @param owner  己方战斗单位
     * @param target 目标战斗单位
     * @return
     */
    public static boolean isInRange(Unit owner, Unit target, Integer range) {
        int distance = getDistance(owner.getPoint(), target.getPoint());
        return range >= distance;
    }

    /**
     * 获取最近的目标
     *
     * @param owner      所有者
     * @param checkRange 是否判断射程
     * @return
     */
    public static Unit getTarget(Unit owner, boolean checkRange, int time) {
        Unit originTarget = owner.getTarget();
        int range = owner.getMinMoveDistance();

        if (originTarget != null && time < 5000 && originTarget.canSelect(time) && originTarget.getFriend() != owner.getFriend()) {
            if (!checkRange) return originTarget;
            int distance = originTarget.getPoint().distance(owner.getPoint());
            if (distance > range) return null;
        }


        Unit traceUnit = owner.getTraceUnit();
        Unit summonUnit = owner.getSummonUnit();
        // 对于召唤单元，暂时只攻击召唤者正在攻击的单元
        out:
        if (summonUnit != null && !summonUnit.isDead()) {
            if (owner.hasState(UnitState.SNEER, time) || owner.hasState(UnitState.ZHUI_JI, time) || owner.hasState(UnitState.CHAOS, time)) {
                break out;
            }
            if (owner.hasState(UnitState.FOLLOW, time) && traceUnit != null && traceUnit.canSelect(time)) {
                break out;
            }
            Unit target = summonUnit.getTarget();
            if (target == null) {
                return null;
            }
            if (!checkRange) {
                return target;
            }
            int distance = target.getPoint().distance(owner.getPoint());
            if (distance <= range) {
                return target;
            }
            return null;
        }


        // 如果有嘲讽状态，那么去攻击嘲讽对象
        if (owner.hasState(UnitState.SNEER, time) || owner.hasState(UnitState.ZHUI_JI, time)) {
            if (traceUnit != null && traceUnit.canSelect(time)) {
                int distance = traceUnit.getPoint().distance(owner.getPoint());
                if (checkRange) {
                    if (distance <= range) {
                        return traceUnit;
                    } else {
                        return null;
                    }
                } else {
                    return traceUnit;
                }
            }
        }

        final List<Unit> enemies = FilterType.ENEMY.filter(owner, time);

        // 跟随代码逻辑硬处理
        if (owner.hasState(UnitState.FOLLOW, time) && traceUnit != null && traceUnit.canSelect(time)) {
            Point traceUnitPoint = traceUnit.getPoint();
            Point ownerPoint = owner.getPoint();

            final Point followPoint = calFollowPoint(ownerPoint, traceUnitPoint);
            if (followPoint == null) {
                return null;
            }

            if (followPoint.distance(ownerPoint) > BattleConstant.FOLLOW_SCOPE) {
                if (checkRange) {
                    return null;
                } else {
                    return traceUnit;
                }
            }
            Unit target = traceUnit.getTarget();
            if (target == null || target == owner || !target.canSelect(time) || !enemies.contains(target)) {
                return null;
            }
            int traceDistance = ownerPoint.distance(target.getPoint());
            if (checkRange) {
                if (traceDistance <= range) {
                    return target;
                }
                return null;
            } else {
                return target;
            }
        }


        int current = Integer.MAX_VALUE;
        Unit minDistance = null;
        for (Unit us : enemies) {
            if (us == null || us == owner || !us.canSelect(time)) {
                continue;
            }
            // 距离
            int distance = RangeHelper.getDistance(owner.getPoint(), us.getPoint());
            if (checkRange) {
                if (distance <= range && distance < current) {
                    current = distance;
                    minDistance = us;
                }
            } else {
                if (distance < current) {
                    current = distance;
                    minDistance = us;
                }
            }
        }
        return minDistance;
    }

    public static Point calFollowPoint(Point ownerPoint, Point traceUnitPoint) {
        final Point followPointA = new Point(traceUnitPoint.x + BattleConstant.SCOPE, traceUnitPoint.y);
        final Point followPointB = new Point(traceUnitPoint.x - BattleConstant.SCOPE, traceUnitPoint.y);
        final Point[] followPoints = {followPointA, followPointB};

        int minDist = Integer.MAX_VALUE;
        Point nearerFollowPoint = null;
        for (Point validPoint : followPoints) {
            if (!validPoint.valid()) {
                continue;
            }
            final int dist = ownerPoint.distance(validPoint);
            if (minDist > dist) {
                minDist = dist;
                nearerFollowPoint = validPoint;
            }
        }
        return nearerFollowPoint;
    }
}
