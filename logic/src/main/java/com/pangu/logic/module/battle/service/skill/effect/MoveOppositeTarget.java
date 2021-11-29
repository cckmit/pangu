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

//移动坐标轴对位
@Component
public class MoveOppositeTarget implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.MOVE_OPPOSITE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final Point point = owner.getPoint();
        final int middle = BattleConstant.MAX_X / 2;
        final int x = point.getX();
        int changeX = x > middle ? BattleConstant.MAX_X - x - BattleConstant.VECTOR_DISTANCE : BattleConstant.MAX_X - x + BattleConstant.VECTOR_DISTANCE;
        owner.move(new Point(changeX, point.getY()));
        skillReport.add(time, owner.getId(), PositionChange.of(point.getX(), point.getY()));
    }
}
