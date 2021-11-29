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
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 跳转到敌人最多的区域
 */
@Component
public class JumpCircle implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.JUMP_CIRCLE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        // 半径
        int range = state.getParam(Integer.class);
        List<Point> points = new ArrayList<>();
        for (Unit unit : owner.getEnemy().getCurrent()) {
            Point point = unit.getPoint();
            points.add(point);
        }
        Point point = owner.getPoint();
        Point targetPoint = BestCircle.calBestPoint(point, points, range, BattleConstant.MAX_X, BattleConstant.MAX_Y, true);
        if (targetPoint == null) {
            return;
        }
        owner.move(targetPoint);

        skillReport.add(time, owner.getId(), PositionChange.of(point.x, point.y));
    }
}
