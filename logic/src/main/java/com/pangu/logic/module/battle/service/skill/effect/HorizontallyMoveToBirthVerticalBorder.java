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

/**
 * 平移至出场位置附近的边界
 */
@Component
public class HorizontallyMoveToBirthVerticalBorder implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.HORIZONTALLY_MOVE_TO_BIRTH_VERTICAL_BORDER;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        // 计算移动目的地
        final Point ownerPoint = owner.getPoint();
        final int modifier = BattleConstant.SCOPE;
        final Point destination = owner.getFriend().isAttacker()
                ? new Point(modifier, ownerPoint.y)
                : new Point(BattleConstant.MAX_X - modifier, ownerPoint.y);

        //  移动
        owner.move(destination, time);
        skillReport.add(time, owner.getId(), PositionChange.of(ownerPoint.x, ownerPoint.y));
    }
}
