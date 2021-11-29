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
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 瞬移至最近的角落
 */
@Component
public class RetreatToNearestCorner implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.RETREAT_TO_NEAREST_CORNER;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        //  获取四个角落的坐标
        final ArrayList<Point> corners = new ArrayList<>(4);
        corners.add(new Point(BattleConstant.SCOPE, 0));
        corners.add(new Point(BattleConstant.SCOPE, BattleConstant.MAX_Y));
        corners.add(new Point(BattleConstant.MAX_X - BattleConstant.SCOPE, 0));
        corners.add(new Point(BattleConstant.MAX_X - BattleConstant.SCOPE, BattleConstant.MAX_Y));

        //  四个角落的坐标按距离施法者当前坐标的距离从小到大排序
        final Point ownerPoint = owner.getPoint();
        final List<Point> sortedCorners = corners.stream().sorted(Comparator.comparingInt(ownerPoint::distance)).collect(Collectors.toList());

        //  施法者移动至最近的角落
        final Point destination = sortedCorners.get(0);
        owner.move(destination, time);
        skillReport.add(time, owner.getId(), PositionChange.of(ownerPoint.x, ownerPoint.y));
    }
}
