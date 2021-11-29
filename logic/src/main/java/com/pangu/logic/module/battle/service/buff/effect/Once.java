package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.CalType;
import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.alter.Alter;
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

/**
 * 添加buff时生效，时间到达移除
 */
@Component
public class Once implements Buff {
    @Override
    public BuffType getType() {
        return BuffType.ONCE;
    }

    @Override
    public boolean add(BuffState state, Unit unit, int time) {
        unit.addBuff(state);
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
            return true;
        }
        PassiveState passiveState = PassiveFactory.initState(passiveId, time);
        unit.addPassive(passiveState, state.getCaster());
        return true;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object o) {

    }

    @Override
    public void remove(BuffState state, Unit unit, int time) {
        if (!unit.removeBuff(state)) {
            return;
        }
        CalValues param = state.getAddition(CalValues.class);
        Map<AlterType, Number> values = param.getValues();
        BuffReport buffReport = state.getBuffReport();
        Context context = new Context(state.getCaster());
        for (Map.Entry<AlterType, Number> entry : values.entrySet()) {
            AlterType alterType = entry.getKey();
            Number number = entry.getValue();
            Alter alter = alterType.getAlter();
            Number reverse = alter.getReverse(number);
            context.addValue(unit, alterType, reverse);
            buffReport.add(time, unit.getId(), new UnitValues(alterType, reverse));
        }
        context.execute(time, buffReport);
        state.setAddition(null);

        OnceParam configParam = state.getParam(OnceParam.class);
        String passiveId = configParam.getPassive();
        if (StringUtils.isEmpty(passiveId)) {
            return;
        }
        PassiveState passiveState = unit.getPassiveStates(passiveId);
        if (passiveState == null) {
            return;
        }
        unit.removePassive(passiveState);
    }
}
