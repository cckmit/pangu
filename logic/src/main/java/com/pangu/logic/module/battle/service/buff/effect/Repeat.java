package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.model.report.values.Mark;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.DefaultAddValueParam;
import com.pangu.logic.module.battle.service.buff.param.RepeatParam;
import com.pangu.logic.module.battle.service.buff.utils.CalTypeHelper;
import com.pangu.logic.module.battle.service.buff.utils.CalValues;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 定时修改属性，buff被移除时还原属性
 */
@Component
public class Repeat implements Buff {
    @Override
    public BuffType getType() {
        return BuffType.REPEAT;
    }

    @Override
    public boolean add(BuffState state, Unit unit, int time) {
        unit.addBuff(state);
        update(state, unit, time, null);
        return true;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object addition) {
        final RepeatParam param = state.getParam(RepeatParam.class);
        final Addition buffAddition = getAddition(state);
        final int updateLimit = param.getUpdateLimit();
        if (updateLimit > 0 && buffAddition.updateTimes >= updateLimit) {
            return;
        }

        // 每回更新时修改的属性
        final BuffReport buffReport = state.getBuffReport();
        final Context context = new Context(state.getCaster());
        final Context hedgeContext = buffAddition.context;
        final String targetId = unit.getId();
        final CalValues calValues = CalTypeHelper.calValues(param.getCalType(), param.getAlters(), state.getCaster(), unit, param.getFactor());
        final Map<AlterType, Number> values = calValues.getValues();
        for (Map.Entry<AlterType, Number> entry : values.entrySet()) {
            AlterType alterType = entry.getKey();
            Number number = entry.getValue();
            context.addValue(unit, alterType, number);
            //记录移除时需要对冲的属性
            hedgeContext.addValue(unit, alterType, alterType.getAlter().getReverse(number));
            buffReport.add(time, targetId, new UnitValues(alterType, number));
        }

        buffAddition.updateTimes++;

        //更新次数已满时，额外修改属性
        final DefaultAddValueParam modValWhenReachLimit = param.getModValWhenReachLimit();
        if (modValWhenReachLimit != null && buffAddition.updateTimes == updateLimit) {
            final CalValues addValues = CalTypeHelper.calValues(modValWhenReachLimit.getCalType(), modValWhenReachLimit.getAlters(), state.getCaster(), unit, modValWhenReachLimit.getFactor());
            for (Map.Entry<AlterType, Number> entry : addValues.getValues().entrySet()) {
                AlterType alterType = entry.getKey();
                Number number = entry.getValue();
                context.addValue(unit, alterType, number);
                //记录移除时需要对冲的属性
                hedgeContext.addValue(unit, alterType, alterType.getAlter().getReverse(number));
                buffReport.add(time, targetId, new UnitValues(alterType, number));
            }
        }

        context.execute(time, buffReport);

        if (param.isReportable()) {
            buffReport.add(time, targetId, new Mark(buffAddition.updateTimes));
        }
    }

    @Override
    public void remove(BuffState state, Unit unit, int time) {
        if (!unit.removeBuff(state)) {
            return;
        }
        Context hedgeContext = getAddition(state).context;
        BuffReport buffReport = state.getBuffReport();
        hedgeContext.execute(time, buffReport);
    }

    public Addition getAddition(BuffState state) {
        Addition addition = state.getAddition(Addition.class);
        if (addition == null) {
            addition = new Addition();
            addition.context = new Context(state.getCaster());
            state.setAddition(addition);
        }
        return addition;
    }

    private static class Addition {
        private Context context;

        /**
         * 累计更新次数
         */
        private int updateTimes;
    }
}
