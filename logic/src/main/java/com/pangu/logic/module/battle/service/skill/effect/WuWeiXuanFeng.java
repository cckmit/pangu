package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.PositionChange;
import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.select.utils.BestCircle;
import com.pangu.logic.module.battle.service.select.select.utils.TwoPointDistanceUtils;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.WuWeiXuanFengParam;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 无畏旋风
 * 挥动大剑，对四周的敌人造成每秒120%物理攻击的伤害，持续4秒（超敌人最密集的区域移动）。
 * 2级：每秒伤害提升至140%物理攻击
 * 3级：每秒伤害提升至160%物理攻击的伤害
 * 4级：持续时间提升至6秒
 */
@Component
public class WuWeiXuanFeng implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.WU_WEI_XUAN_FENG;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        int loopTimes = context.getLoopTimes();
        int effectTime = skillState.getExecuteTimes() * skillState.getExecuteInterval();
        if (loopTimes == 1 || loopTimes == skillState.getExecuteTimes()) {
            if (loopTimes == 1) {
                owner.addState(UnitState.BA_TI, time + effectTime);
            } else {
                owner.removeState(UnitState.BA_TI, time + effectTime);
            }
        }
        List<Point> points = new ArrayList<>();
        for (Unit unit : owner.getEnemy().getCurrent()) {
            Point point = unit.getPoint();
            points.add(point);
        }
        WuWeiXuanFengParam param = state.getParam(WuWeiXuanFengParam.class);
        Point ownerPoint = owner.getPoint();
        Point targetPoint = BestCircle.calBestPoint(ownerPoint, points, param.getCircleRadius(), BattleConstant.MAX_X, BattleConstant.MAX_Y, true);
        if (targetPoint == null) {
            targetPoint = owner.getTarget().getPoint();
        }
        Point nextMovePoint = TwoPointDistanceUtils.getNearStartPoint(ownerPoint, param.getStep(), targetPoint);
        owner.move(nextMovePoint);
        skillReport.add(time, owner.getId(), PositionChange.of(ownerPoint.x, ownerPoint.y));
    }
}
