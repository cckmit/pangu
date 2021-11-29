package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.buff.param.DefaultAddValueParam;
import com.pangu.logic.module.battle.service.buff.utils.CalTypeHelper;
import com.pangu.logic.module.battle.service.buff.utils.CalValues;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.param.BuffUpdateParam;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.select.select.utils.BestCircle;
import com.pangu.logic.module.battle.service.select.select.utils.Circle;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.AreaParam;
import com.pangu.logic.module.battle.service.skill.param.EffectAreaParam;
import com.pangu.logic.module.battle.service.skill.param.EffectAreaParam.Strategy;
import com.pangu.logic.module.battle.service.skill.param.EnhancedAreaParam;
import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import com.pangu.logic.module.battle.service.skill.utils.SkillUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 对某个固定区域持续施加影响
 */
@Component
public class EffectArea implements SkillEffect {
    @Autowired
    private BuffUpdate buffUpdate;

    @Override
    public EffectType getType() {
        return EffectType.EFFECT_AREA;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final EffectAreaParam param = state.getParam(EffectAreaParam.class);

        /* 首次执行效果时，构建区域并缓存*/
        List<AreaParam> areaParams = context.getRootSkillEffectAction().getAddition(List.class);
        if (areaParams == null) {
            final List<Unit> targets = TargetSelector.select(owner, param.getAnchorTargetId(), time);
            areaParams = new ArrayList<>();
            for (Map.Entry<Strategy, EnhancedAreaParam> entry : param.getStrategy2BuildParam().entrySet()) {
                final Strategy strategy = entry.getKey();
                final EnhancedAreaParam buildParam = entry.getValue();
                final int radius = buildParam.getR();
                if (strategy == Strategy.TARGET_CIRCLE) {
                    for (Unit unit : targets) {
                        final Point point = unit.getPoint();
                        final Circle circle = new Circle(point.x, point.y, radius);
                        areaParams.add(AreaParam.from(circle));
                    }
                }
                if (strategy == Strategy.BEST_CIRCLE) {
                    final List<Point> targetPoints = targets.stream().map(Unit::getPoint).collect(Collectors.toList());
                    final Point bestCenter = BestCircle.calBestPoint(owner.getPoint(), targetPoints, radius, BattleConstant.MAX_X, BattleConstant.MAX_Y, false);
                    final Circle circle = new Circle(bestCenter.x, bestCenter.y, radius);
                    final AreaParam circleArea = AreaParam.from(circle);
                    areaParams.add(circleArea);
                    skillReport.setAreaParam(circleArea, time);
                }
            }
            context.getRootSkillEffectAction().setAddition(areaParams);
        }

        /* 应用自定义过滤器和区域参数筛选出符合条件的目标*/
        final List<Unit> targets = param.getFilter().filter(owner, time);
        final Set<Unit> inArea = new HashSet<>(6);
        for (AreaParam areaParam : areaParams) {
            inArea.addAll(areaParam.inArea(targets));
        }

        /* 修改其属性*/
        final DefaultAddValueParam valModParam = param.getValModParam();
        if (valModParam != null) {
            for (Unit unit : inArea) {
                final CalValues calValues = CalTypeHelper.calValues(valModParam.getCalType(), valModParam.getAlters(), owner, unit, valModParam.getFactor());
                for (Map.Entry<AlterType, Number> entry : calValues.getValues().entrySet()) {
                    final AlterType alterType = entry.getKey();
                    final Number value = entry.getValue();
                    context.addValue(unit, alterType, value);
                    skillReport.add(time, unit.getId(), new UnitValues(alterType, value));
                }
            }
        }

        /* 为其添加异常状态*/
        final StateAddParam stateAddParam = param.getStateAddParam();
        if (stateAddParam != null) {
            for (Unit unit : inArea) {
                SkillUtils.addState(owner, unit, stateAddParam.getState(), time, time + stateAddParam.getTime(), skillReport, context);
            }
        }

        /* 更新buff*/
        final BuffUpdateParam buffUpdateParam = param.getBuff();
        if (buffUpdateParam != null) {
            for (Unit unit : inArea) {
                buffUpdate.doBuffUpdate(buffUpdateParam, owner, unit, skillReport, time);
            }
        }
    }
}
