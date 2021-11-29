package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.PositionChange;
import com.pangu.logic.module.battle.service.core.*;
import com.pangu.logic.module.battle.service.select.select.utils.Circle;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 水平方向对齐target的移动效果，优先选取单位数量较为稀疏的一侧。
 */
@Component
public class TargetHorizontalAlignMove implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.FACE_TARGET_HORIZONTAL_ALIGN_MOVE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final Point targetPoint = target.getPoint();

        //  计算自身与目标的距离
        final Point ownerPoint = owner.getPoint();
        final int distance = ownerPoint.distance(targetPoint);

        //  筛选出人群最稀疏的位置
        Point rightPoint = new Point(targetPoint.x + distance, targetPoint.y);
        Point leftPoint = new Point(targetPoint.x - distance, targetPoint.y);
        final Integer radius = state.getParam(Integer.class);
        final List<Unit> all = FilterType.ALL.filter(owner, time);
        final Circle rightCircle = new Circle(rightPoint.x, rightPoint.y, radius);
        final Circle leftCircle = new Circle(leftPoint.x, leftPoint.y, radius);
        final List<Unit> countInRightCircle = new ArrayList<>();
        final List<Unit> countInLeftCircle = new ArrayList<>();
        for (Unit unit : all) {
            if (unit == target || unit == owner) {
                continue;
            }
            if (rightCircle.inShape(unit.getPoint().x, unit.getPoint().y)) {
                countInRightCircle.add(unit);
            }
            if (leftCircle.inShape(unit.getPoint().x, unit.getPoint().y)) {
                countInLeftCircle.add(unit);
            }
        }

        //瞬移
        final int leftCircleSize = countInLeftCircle.size();
        final int rightCircleSize = countInRightCircle.size();

        Point pointToMove;
        if (leftCircleSize < rightCircleSize) {
            pointToMove = leftPoint;
        } else if (leftCircleSize == rightCircleSize) {
            //当目标两侧同样稀疏时，移动至距离施法者较近的一侧
            pointToMove = rightPoint.distance(ownerPoint) > leftPoint.distance(ownerPoint) ? leftPoint : rightPoint;
        } else {
            pointToMove = rightPoint;
        }
        owner.move(pointToMove, time);
        skillReport.add(time, owner.getId(), PositionChange.of(ownerPoint.x, ownerPoint.y));
    }
}
