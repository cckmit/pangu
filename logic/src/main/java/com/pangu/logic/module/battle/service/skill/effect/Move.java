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
import com.pangu.logic.module.battle.service.select.select.utils.TwoPointDistanceUtils;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import org.springframework.stereotype.Component;

/**
 * 修改角色坐标
 */
@Component
public class Move implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.MOVE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final Point destPoint = state.getParam(Point.class);
        final Point targetPoint = target.getPoint();
        final Point modifiedDestPoint = TwoPointDistanceUtils.getNearStartPoint(targetPoint, BattleConstant.SCOPE, destPoint);
        target.move(modifiedDestPoint, time);
        skillReport.add(time, target.getId(), PositionChange.of(targetPoint.x, targetPoint.y));
    }
}
