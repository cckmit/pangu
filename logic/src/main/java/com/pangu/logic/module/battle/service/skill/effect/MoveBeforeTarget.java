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
 * 移动到目标身前
 *
 * @author Kubby
 */
@Component
public class MoveBeforeTarget implements SkillEffect {

    @Override
    public EffectType getType() {
        return EffectType.MOVE_BEFORE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time,
                        SkillState skillState, Context context) {
        final Point ownerPoint = owner.getPoint();
        final Point movePoint = TwoPointDistanceUtils
                .getNearEndPointDistance(ownerPoint, target.getPoint(), -state.getParam(Integer.class));
        owner.move(movePoint);
        skillReport.add(time, owner.getId(), PositionChange.of(ownerPoint.getX(), ownerPoint.getY()));
    }

}
