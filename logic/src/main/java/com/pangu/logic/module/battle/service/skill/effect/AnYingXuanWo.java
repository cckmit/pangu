package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.PositionChange;
import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.select.select.utils.TwoPointDistanceUtils;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.AnYingXuanWoParam;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 暗影漩涡
 * 在战场中心召唤暗影漩涡，旋涡持续5秒，同时会持续的吸引范围内的敌人向中心靠拢并造成每秒70%攻击力的伤害。
 * 2级:伤害提升至75%攻击力
 * 3级:伤害提升至80%攻击力
 * 4级：降低敌人100%的能量回复
 */
@Component
public class AnYingXuanWo implements SkillEffect {

    @Autowired
    private HpMagicDamage magicDamageEffect;

    @Override
    public EffectType getType() {
        return EffectType.AN_YING_XUAN_WO;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        AnYingXuanWoParam param = state.getParam(AnYingXuanWoParam.class);

        String targetId = param.getTargetId();
        List<Unit> select = TargetSelector.select(owner, targetId, time);
        int distance = param.getDistance() * skillState.getExecuteTimes();
        state.setParamOverride(new DamageParam(param.getFactor()));
        Point point = new Point(BattleConstant.MAX_X / 2, BattleConstant.MAX_Y / 2);
        String buffId = param.getBuff();
        boolean needAddBuff = context.getLoopTimes() == 1 && StringUtils.isNotEmpty(buffId);
        int executeInterval = skillState.getExecuteInterval() * skillState.getExecuteTimes();
        for (Unit unit : select) {
            magicDamageEffect.execute(state, owner, unit, skillReport, time, skillState, context);
            if (needAddBuff) {
                BuffFactory.addBuff(buffId, owner, unit, time, skillReport, null);
            }
            if (unit.hasState(UnitState.BA_TI, time)) {
                continue;
            }
            if (context.getLoopTimes() != 1) {
                continue;
            }
            Point unitPoint = unit.getPoint();
            int curDistance = unitPoint.distance(point);
            Point targetPosition;
            final int modifier = param.getModifier();
            if (curDistance <= modifier) {
                targetPosition = unitPoint;
            } else {
                int dragDistance = distance;
                if (distance >= curDistance) {
                    dragDistance = curDistance - modifier;
                }
                targetPosition = TwoPointDistanceUtils.getNearStartPoint(unitPoint, dragDistance, point);
            }
            unit.move(targetPosition);
            skillReport.add(time, unit.getId(), PositionChange.of(unitPoint.x, unitPoint.y));
            if (executeInterval > 0) {
                unit.addState(UnitState.NO_MOVE, executeInterval + time);
            }
        }
        state.setParamOverride(null);
    }
}
