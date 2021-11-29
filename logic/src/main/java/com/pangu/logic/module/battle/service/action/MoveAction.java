package com.pangu.logic.module.battle.service.action;

import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.MoveReport;
import com.pangu.logic.module.battle.service.Battle;
import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.RangeHelper;
import com.pangu.logic.module.battle.service.select.RoleSkinFactory;
import com.pangu.logic.module.battle.service.select.select.utils.TwoPointDistanceUtils;
import com.pangu.logic.module.battle.service.utils.AStar;
import com.pangu.logic.module.battle.service.utils.Rect;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.BitSet;
import java.util.List;

/**
 * 战斗单元移动
 */
@Slf4j
@Getter
public class MoveAction implements Action {

    private final int MOVE_TIME_RECORD_REPORT = 4;
    private int time;
    private final Unit owner;

    // 移动多少次后，添加一个战报
    private int times;

    // 花费时间
    private int costTime;
    private boolean forceAstar;
    private boolean direct = false;

    /**
     * 构造
     *
     * @param time  时间
     * @param owner 所有者
     */
    public MoveAction(int time, Unit owner) {
        this.time = time;
        this.owner = owner;
    }

    @Override
    public void execute() {
        if (owner.hasState(UnitState.NO_MOVE, time)) {
            owner.reset(time + 1000);
            return;
        }
        // 设置行动状态
        Unit target = RangeHelper.getTarget(owner, false, time);
        owner.setTarget(target);
        // 没有选择到目标
        if (target == null) {
            owner.reset(time + 1000);
            return;
        }
        Point ownerPoint = owner.getPoint();
        Point targetPoint = target.getPoint();
        int distance = (int) Math.sqrt((targetPoint.x - ownerPoint.x) * (targetPoint.x - ownerPoint.x)
                + (targetPoint.y - ownerPoint.y) * (targetPoint.y - ownerPoint.y));

        int movePerMill = BattleConstant.MoveIntervalMill;
        // 判断 Follow 跟随逻辑处理
        long speed = owner.getValue(UnitValue.SPEED);

        if (speed <= 0) {
            owner.reset(time + 1000);
            return;
        }

        if (owner.hasState(UnitState.FOLLOW, time)) {
            if (target != owner.getTraceUnit()) {
                owner.reset(time + BattleConstant.MoveIntervalMill);
                return;
            }
            final Point followPoint = RangeHelper.calFollowPoint(ownerPoint, targetPoint);
            if (ownerPoint.distance(followPoint) > BattleConstant.FOLLOW_SCOPE) {
                Point movePoint = TwoPointDistanceUtils.getNearStartPoint(ownerPoint, (int) speed, followPoint);
                // 变更位置
                move(target, movePerMill, movePoint);
                return;
            }
            owner.reset(time + BattleConstant.MoveIntervalMill);
            return;
        }

        // 攻击范围内有目标,退出移动
        int shotMinDistance = owner.getMinMoveDistance();
        if (distance <= shotMinDistance) {
            owner.reset(time);
            return;
        }
        if (!forceAstar && (direct || distance > BattleConstant.VECTOR_DISTANCE)) {
//            long fast = speed << 1;
//            if (fast < BattleConstant.VECTOR_DISTANCE) {
//                speed = speed << 1;
//                movePerMill = movePerMill << 1;
//            }
            Point movePoint = TwoPointDistanceUtils.getNearStartPoint(ownerPoint, (int) speed, targetPoint);
            // 变更位置
            move(target, movePerMill, movePoint);
            return;
        }
        int maxX = BattleConstant.MAX_X;
        int maxY = BattleConstant.MAX_Y;
        BitSet map = new BitSet(maxX * maxY);
        AStar aStar = new AStar(map, maxX, maxY);

        // 初始化障碍物
        initMap(aStar.getNodes(), owner, target, owner.getFriend().getCurrent(), maxX, maxY);
        initMap(aStar.getNodes(), owner, target, owner.getEnemy().getCurrent(), maxX, maxY);

        List<Point> path = aStar.findPath(ownerPoint, targetPoint, shotMinDistance);
        if (path.isEmpty()) {
            if (times > 0 && costTime > 0) {
                Battle battle = owner.getBattle();
                battle.addReport(MoveReport.of(time, owner, costTime));
                times = 0;
                this.costTime = 0;
            }
            this.time += 50;
            this.direct = true;
            this.forceAstar = false;
            owner.updateAction(this);
            return;
        }
        if (path.size() > speed) {
            path = path.subList(0, (int) speed);
        } else {
            movePerMill = (int) (movePerMill * (1.0 * path.size() / speed));
        }
        forceAstar = true;
        // 直接走到最后一个位置(将走路路线生成战报返回)
        Point node = path.get(path.size() - 1);
        // 变更位置
        move(target, movePerMill, node);
    }

    private void move(Unit target, int movePerMill, Point movePoint) {
        ++times;
        this.costTime += movePerMill;
        owner.move(movePoint);
        if (times >= MOVE_TIME_RECORD_REPORT) {
            times = 0;
            Battle battle = owner.getBattle();
            battle.addReport(MoveReport.of(time, owner, costTime));
            this.costTime = 0;
        }
        generateNext(movePerMill, target);
    }

    private void generateNext(int moveTime, Unit target) {
        int distance = owner.getPoint().distance(target.getPoint());
        // 射程内没有目标
        if (distance > owner.getMinMoveDistance()) {
            time += moveTime;
            owner.updateAction(this);
        } else {
            owner.setTarget(target);
            owner.reset(time + moveTime);
            if (times != 0) {
                Battle battle = owner.getBattle();
                battle.addReport(MoveReport.of(time, owner, costTime));
            }
        }
    }

    private void initMap(BitSet bitSet, Unit owner, Unit target, List<Unit> allUnit, int maxX, int maxY) {
        Point ownerPoint = owner.getPoint();
        Point targetPoint = target.getPoint();
        for (Unit unit : allUnit) {
            if (unit == owner || unit == target) {
                continue;
            }
            if (unit.isDead()) {
                continue;
            }
            Point position = unit.getPoint();
            int x = position.getX();
            int y = position.getY();
//            bitSet.set(Utils.squareIndex(x, y, maxX));
            int modelId = unit.getModel().getModel();
            int scope = RoleSkinFactory.getScope(modelId);
            fill(x, y, bitSet, scope, maxX, maxY, ownerPoint, targetPoint);
        }
    }

    /* 将路径中障碍物（非死亡单元）与移动中主体的碰撞区域在AStar的节点列表中设为true，以便在选择可移动区域时进行排除*/
    void fill(int x, int y, BitSet bitSet, int scope, int maxX, int maxY, Point ownerPoint, Point targetPoint) {
        int square = scope >> 1;
        if (square <= 0) {
            return;
        }
        Rect rect = new Rect(x, y, scope);
        Rect ownerRect = rect.cross(ownerPoint.x, ownerPoint.y);
        int OminX = 0;
        int OminY = 0;

        int OmaxX = 0;
        int OmaxY = 0;
        if (ownerRect != null) {
            OminX = ownerRect.minX;
            OminY = ownerRect.minY;
            OmaxX = ownerRect.maxX;
            OmaxY = ownerRect.maxY;
        }
        Rect targetRect = rect.cross(targetPoint.x, targetPoint.y);
        int TminX = 0;
        int TminY = 0;

        int TmaxX = 0;
        int TmaxY = 0;
        if (targetRect != null) {
            TminX = targetRect.minX;
            TminY = targetRect.minY;
            TmaxX = targetRect.maxX;
            TmaxY = targetRect.maxY;
        }
        int oX = ownerPoint.x;
        int tX = targetPoint.x;
        int cyMax = Math.min(y + square, maxY);
        int cxMax = Math.min(x + square, maxX);
        final int bxMin = Math.max(1, x - square);
        for (int curY = Math.max(1, y - square); curY <= cyMax; ++curY) {
            for (int curX = bxMin; curX <= cxMax; ++curX) {
//                if (oX < tX) {
//                    if (curX <= oX) {
//                        continue;
//                    }
//                } else {
//                    if (curX >= oX) {
//                        continue;
//                    }
//                }
                if (curX >= OminX && curX <= OmaxX && curY >= OminY && curY <= OmaxY) {
                    continue;
                }

                if (curX >= TminX && curX <= TmaxX && curY >= TminY && curY <= TmaxY) {
                    continue;
                }

                bitSet.set(curY * maxX + curX);
            }
        }
    }

    void print(BitSet map, int maxX, int maxY) {
        for (int y = 0; y < maxY; ++y) {
            System.out.print(y + " ");
            for (int x = 0; x < maxX; ++x) {
                int index = y * maxX + x;
                boolean b = map.get(index);
                if (b) {
                    System.out.print(" B ");
                } else {
                    System.out.print(" _ ");
                }
            }
            System.out.println();
        }
    }
}
