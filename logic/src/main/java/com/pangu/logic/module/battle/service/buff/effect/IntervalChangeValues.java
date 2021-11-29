package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.alter.MpAlter;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.DefaultAddValueParam;
import com.pangu.logic.module.battle.service.buff.utils.CalTypeHelper;
import com.pangu.logic.module.battle.service.buff.utils.CalValues;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.framework.utils.reflect.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 间隔生效 改变属性
 */
@Component
public class IntervalChangeValues implements Buff {

    @Override
    public BuffType getType() {
        return BuffType.INTERVAL_VALUES;
    }


    @Override
    public void update(BuffState state, Unit unit, int time, Object o) {
        final DefaultAddValueParam param = state.getParam(DefaultAddValueParam.class);
        if (!condVerify(state, unit, param.getUpdateCond(), time)) {
            return;
        }

        BuffReport buffReport = state.getBuffReport();
        Context context = new Context(state.getCaster());
        final String selectId = param.getTargetId();
        final List<Unit> targets;
        if (StringUtils.isEmpty(selectId)) {
            targets = Collections.singletonList(unit);
        } else {
            targets = TargetSelector.select(unit, selectId, time);
        }
        for (Unit target : targets) {
            String targetId = target.getId();

            CalValues calValues = CalTypeHelper.calValues(param.getCalType(), param.getAlters(), state.getCaster(), target, param.getFactor());
            Map<AlterType, Number> values = calValues.getValues();
            final Number mpChange = values.remove(AlterType.MP);
            if (mpChange != null) {
                final long mpChangeForReport = MpAlter.calMpChange(target, mpChange.longValue());
                context.addValue(target, AlterType.MP, mpChange);
                buffReport.add(time, targetId, new UnitValues(AlterType.MP, mpChangeForReport));
            }
            for (Map.Entry<AlterType, Number> entry : values.entrySet()) {
                AlterType alterType = entry.getKey();
                Number number = entry.getValue();
                context.addValue(target, alterType, number);
                buffReport.add(time, targetId, new UnitValues(alterType, number));
            }
        }
        context.execute(time, buffReport);
    }

}
