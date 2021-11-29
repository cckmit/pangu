package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.CalValuesByTargetSizeParam;
import com.pangu.logic.module.battle.service.buff.utils.CalTypeHelper;
import com.pangu.logic.module.battle.service.buff.utils.CalValues;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 周围每存在一个敌方英雄则自己的防御力提升3%
 */
@Component
public class CalValuesByTargetSize implements Buff {
    @Override
    public BuffType getType() {
        return BuffType.CAL_VALUES_BY_TARGET_SIZE;
    }

    @Override
    public boolean add(BuffState state, Unit unit, int time) {
        unit.addBuff(state);
        //统计依据
        final CalValuesByTargetSizeParam param = state.getParam(CalValuesByTargetSizeParam.class);
        final int calTimes = TargetSelector.select(unit, param.getTarget(), time).size();

        //根据统计依据成倍修改自身属性
        final Context context = new Context(unit);
        CalValues calValues = CalTypeHelper.calValues(param.getCalType(), param.getAlters(), state.getCaster(), unit, param.getFactor());
        Map<AlterType, Number> values = calValues.getValues();
        BuffReport buffReport = state.getBuffReport();

        for (Map.Entry<AlterType, Number> entry : values.entrySet()) {
            AlterType alterType = entry.getKey();
            double totalValueChange = entry.getValue().doubleValue() * calTimes;
            context.addValue(unit, alterType, totalValueChange);
            buffReport.add(time, unit.getId(), new UnitValues(alterType, totalValueChange));
        }

        context.execute(time, state.getBuffReport());

        //缓存当前统计依据
        final Addition addition = new Addition();
        addition.calValues = calValues;
        addition.preCalTimes = calTimes;
        state.setAddition(addition);

        return true;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object addition) {
        //统计依据
        final CalValuesByTargetSizeParam param = state.getParam(CalValuesByTargetSizeParam.class);
        final int curCalTimes = TargetSelector.select(unit, param.getTarget(), time).size();
        final Addition stateAddition = state.getAddition(Addition.class);
        final int preCalTimes = stateAddition.preCalTimes;

        //监听存活单位变化量
        final int delta = curCalTimes - preCalTimes;
        //无变化不做任何处理
        if (delta == 0) {
            return;
        }
        stateAddition.preCalTimes = curCalTimes;

        //根据变化量修改当前属性
        final Context context = new Context(unit);
        Map<AlterType, Number> values = stateAddition.calValues.getValues();

        BuffReport buffReport = state.getBuffReport();
        for (Map.Entry<AlterType, Number> entry : values.entrySet()) {
            AlterType alterType = entry.getKey();
            double totalValueChange = entry.getValue().doubleValue() * delta;
            context.addValue(unit, alterType, totalValueChange);
            buffReport.add(time, unit.getId(), new UnitValues(alterType, totalValueChange));
        }

        context.execute(time, state.getBuffReport());
    }

    private static class Addition {
        private CalValues calValues;
        private int preCalTimes;
    }
}
