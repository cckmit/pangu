package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.ValModCounterParam;
import com.pangu.logic.module.battle.service.buff.utils.CalTypeHelper;
import com.pangu.logic.module.battle.service.buff.utils.CalValues;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import com.pangu.logic.module.battle.service.skill.utils.SkillUtils;
import org.springframework.stereotype.Component;

import java.util.Map;


/**
 * 当达到计数器最大值时，清零计数并执行一系列属性修改
 */
@Component
public class ValModCounter extends Counter {
    @Override
    protected void init() {
        super.setCallback(new Callback() {
            @Override
            public void exeWhenCountMax(BuffState state, Unit unit, int time) {
                final ValModCounterParam param = state.getParam(ValModCounterParam.class);

                final Unit caster = state.getCaster();
                final Context context = new Context(caster);
                final BuffReport buffReport = state.getBuffReport();
                boolean exec = false;
                if (param.getCalType() != null) {
                    final CalValues calValues = CalTypeHelper.calValues(param.getCalType(), param.getAlters(), caster, unit, param.getFactor());
                    for (Map.Entry<AlterType, Number> entry : calValues.getValues().entrySet()) {
                        final AlterType alterType = entry.getKey();
                        final Number value = entry.getValue();
                        context.addValue(unit, alterType, value);
                        buffReport.add(time, unit.getId(), new UnitValues(alterType, value));
                    }
                    exec = true;
                }

                final StateAddParam stateAddParam = param.getStateAddParam();
                if (stateAddParam != null) {
                    if (param.isNeedStateReport()) {
                        SkillUtils.addState(caster, unit, stateAddParam.getState(), time, time + state.getTime(), buffReport, context);
                    } else {
                        SkillUtils.addState(caster, unit, stateAddParam.getState(), time, time + state.getTime(), null, context);
                    }
                    exec = true;
                }

                if (exec) {
                    context.execute(time, buffReport);
                }

                state.setAddition(0);
            }
        });
    }

    @Override
    public BuffType getType() {
        return BuffType.VAL_MOD_COUNTER;
    }
}
