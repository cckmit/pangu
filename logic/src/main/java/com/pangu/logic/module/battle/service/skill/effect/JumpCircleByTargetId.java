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
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.select.select.utils.BestCircle;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.JumpCircleByTargetIdParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 根据指定选择策略筛选出的目标计算最佳圆圈
 */
@Component
public class JumpCircleByTargetId implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.JUMP_CIRCLE_BY_TARGET_ID;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final JumpCircleByTargetIdParam param = state.getParam(JumpCircleByTargetIdParam.class);

        //根据策略筛选作为计算依据的目标
        final List<Unit> targets = TargetSelector.select(owner, param.getTargetId(), time);

        //计算最佳圆心
        final List<Point> points = targets.stream().map(Unit::getPoint).collect(Collectors.toList());
        final Point ownerPoint = owner.getPoint();
        Point targetPoint = BestCircle.calBestPoint(ownerPoint, points, param.getRadius(), BattleConstant.MAX_X, BattleConstant.MAX_Y, true);

        //移动至目标区域
        if (targetPoint == null) {
            return;
        }
        owner.move(targetPoint,time);

        skillReport.add(time, owner.getId(), PositionChange.of(ownerPoint.x, ownerPoint.y));
    }
}
