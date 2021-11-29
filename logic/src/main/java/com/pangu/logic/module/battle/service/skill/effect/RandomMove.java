package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.PositionChange;
import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.select.utils.BestCircle;
import com.pangu.logic.module.battle.service.select.select.utils.Circle;
import com.pangu.logic.module.battle.service.select.select.utils.Rectangle;
import com.pangu.logic.module.battle.service.select.select.utils.TwoPointDistanceUtils;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.WuWeiXuanFengParam;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 随机移动效果
 * 目前仅实现朝敌人最密集的区域内的随机一点进行移动
 */
@Component
public class RandomMove implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.RANDOM_MOVE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final Point[] bgPs = {new Point(0, BattleConstant.MAX_Y), new Point(BattleConstant.MAX_X, BattleConstant.MAX_Y), new Point(BattleConstant.MAX_X, 0), new Point(0, 0)};
        Point nextMovePoint;
        final Rectangle bg = new Rectangle(bgPs);

        List<Point> points = new ArrayList<>();
        for (Unit unit : owner.getEnemy().getCurrent()) {
            Point point = unit.getPoint();
            points.add(point);
        }
        WuWeiXuanFengParam param = state.getParam(WuWeiXuanFengParam.class);
        Point ownerPoint = owner.getPoint();
        Point bestCircleCenter = BestCircle.calBestPoint(ownerPoint, points, param.getCircleRadius(), BattleConstant.MAX_X, BattleConstant.MAX_Y, false);
        if (bestCircleCenter == null) {
            bestCircleCenter = owner.getTarget().getPoint();
        }
        //均匀、随机生成给定圆内的点
        final Circle circle = new Circle(bestCircleCenter.x, bestCircleCenter.y, param.getCircleRadius());
        Point targetPoint = null;
        do {
            final int x = RandomUtils.betweenInt(bestCircleCenter.x - param.getCircleRadius(), bestCircleCenter.x + param.getCircleRadius(), false);
            final int y = RandomUtils.betweenInt(bestCircleCenter.y - param.getCircleRadius(), bestCircleCenter.y + param.getCircleRadius(), false);
            if (circle.inShape(x, y)) targetPoint = new Point(x, y);
        } while (targetPoint == null);

        nextMovePoint = TwoPointDistanceUtils.getNearStartPoint(ownerPoint, param.getStep(), targetPoint);

        owner.move(nextMovePoint, time);
        skillReport.add(time, owner.getId(), PositionChange.of(ownerPoint.x, ownerPoint.y));
    }
}
