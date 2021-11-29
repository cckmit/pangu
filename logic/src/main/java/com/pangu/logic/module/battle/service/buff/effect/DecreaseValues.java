package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.DecreaseValuesParam;
import com.pangu.logic.module.battle.service.buff.utils.CalTypeHelper;
import com.pangu.logic.module.battle.service.buff.utils.CalValues;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 初始获得一个属性 之后逐渐递减
 */
@Component
public class DecreaseValues implements Buff {

    @Override
    public BuffType getType() {
        return BuffType.DECREASE_VALUE;
    }

    @Override
    public boolean add(BuffState state, Unit unit, int time) {
        final DecreaseValuesParam param = state.getParam(DecreaseValuesParam.class);
        if (param == null) {
            return false;
        }
        unit.addBuff(state);
        CalValues calValues = CalTypeHelper.calValues(param.getCalType(), param.getInitValues(), state.getCaster(), unit, param.getFactor());
        Map<AlterType, Number> values = calValues.getValues();
        BuffReport buffReport = state.getBuffReport();
        Context context = new Context(state.getCaster());
        for (Map.Entry<AlterType, Number> entry : values.entrySet()) {
            AlterType alterType = entry.getKey();
            Number number = entry.getValue();
            context.addValue(unit, alterType, number);
            buffReport.add(time, unit.getId(), new UnitValues(alterType, number));
        }
        context.execute(time, buffReport);
        return true;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object o) {
        CalValues calValues = state.getAddition(CalValues.class);
        if (calValues == null) {
            final DecreaseValuesParam param = state.getParam(DecreaseValuesParam.class);
            if (param == null) {
                return;
            }
            calValues = CalTypeHelper.calValues(param.getCalType(), param.getDecreaseValue(), state.getCaster(), unit, param.getFactor());
            state.setAddition(calValues);
        }
        Map<AlterType, Number> values = calValues.getValues();
        BuffReport buffReport = state.getBuffReport();
        Context context = new Context(state.getCaster());
        for (Map.Entry<AlterType, Number> entry : values.entrySet()) {
            AlterType alterType = entry.getKey();
            Number number = entry.getValue();
            context.addValue(unit, alterType, number);
            buffReport.add(time, unit.getId(), new UnitValues(alterType, number));
        }
        context.execute(time, buffReport);
    }

}
