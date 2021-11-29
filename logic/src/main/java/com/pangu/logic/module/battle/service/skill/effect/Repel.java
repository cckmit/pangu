package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.PositionChange;
import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.action.Action;
import com.pangu.logic.module.battle.service.action.SkillAction;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import org.springframework.stereotype.Component;

/**
 * 击退
 */
@Component
public class Repel implements SkillEffect {

    @Override
    public EffectType getType() {
        return EffectType.REPEL;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final PositionChange positionReport = new PositionChange();
        final boolean repelSuccess = doRepel(state.getParam(Integer.class), owner, target, time, positionReport);
        if (!repelSuccess) {
            return;
        }
        skillReport.add(time, target.getId(), positionReport);
    }

    public boolean doRepel(int repelDistance, Unit owner, Unit target, int startTime, PositionChange positionReport) {
        if (target.hasState(UnitState.BA_TI, startTime)) {
            return false;
        }
        int distance = Math.min(BattleConstant.MAX_X, repelDistance);
        Point point = target.getPoint();

        Point ownerPoint = owner.getPoint();

        Point targetPoint = calDestination(distance, point, ownerPoint);

        final int moveTime = distance * 100 / 40;
        final int stopTime = startTime + moveTime;

        final Action targetAction = target.getAction();
        if (targetAction instanceof SkillAction) {
            ((SkillAction) targetAction).broken(startTime);
        } else {
            target.reset(startTime);
        }

        target.addState(UnitState.DISABLE, stopTime);
        positionReport.setStopTime(stopTime);

        target.move(targetPoint);
        positionReport.setX(point.x);
        positionReport.setY(point.y);

        return true;
    }

    public Point calDestination(int distance, Point targetPoint, Point ownerPoint) {
        Point destination;
        if (ownerPoint.getX() <= targetPoint.getX()) {
            destination = new Point(targetPoint.getX() + distance, targetPoint.getY());
            if (destination.x > BattleConstant.MAX_X) {
                destination.x = BattleConstant.MAX_X - 10;
            }
        } else {
            destination = new Point(targetPoint.getX() - distance, targetPoint.getY());
            if (destination.x < 0) {
                destination.x = 0;
            }
        }
        return destination;
    }
}
