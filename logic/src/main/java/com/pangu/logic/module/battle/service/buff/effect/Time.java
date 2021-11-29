package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.CalType;
import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.OnceParam;
import com.pangu.logic.module.battle.service.buff.utils.CalTypeHelper;
import com.pangu.logic.module.battle.service.buff.utils.CalValues;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveFactory;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class Time implements Buff {
    @Override
    public BuffType getType() {
        return BuffType.TIME;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object o) {

    }

    @Override
    public void remove(BuffState state, Unit unit, int time) {
        if (!unit.removeBuff(state)) {
            return;
        }
        OnceParam param = state.getParam(OnceParam.class);
        // 迭代处理BUFF中的值变更信息
        CalType calType = param.getCalType();
        CalValues calValues = CalTypeHelper.calValues(calType, param.getAlters(), state.getCaster(), unit, param.getFactor());
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
        state.setAddition(calValues);

        String passiveId = param.getPassive();
        if (StringUtils.isEmpty(passiveId)) {
            return;
        }
        PassiveState passiveState = PassiveFactory.initState(passiveId, time);
        unit.addPassive(passiveState, state.getCaster());
    }
}
