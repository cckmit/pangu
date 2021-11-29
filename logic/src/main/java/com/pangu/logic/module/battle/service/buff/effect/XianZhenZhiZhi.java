package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.alter.Alter;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.DefaultAddValueParam;
import com.pangu.logic.module.battle.service.buff.utils.CalTypeHelper;
import com.pangu.logic.module.battle.service.buff.utils.CalValues;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 陷阵之志 进入敌方半场时添加自身属性
 */
@Component
public class XianZhenZhiZhi implements Buff {

    private static final String ADD_VALUES = "addValues";

    private static final String ADD_FLAG = "flag";


    @Override
    public BuffType getType() {
        return BuffType.XZZZ;
    }

    @Override
    public boolean add(BuffState state, Unit unit, int time) {
        unit.addBuff(state);
        //半场距离
        final int distance = BattleConstant.MAX_X / 2;
        final boolean attacker = unit.getFriend().isAttacker();
        state.setAddition(new HashMap<String, Object>());
        //半径内是否有敌方
        if (inTargetPoint(distance, unit, attacker)) {
            addValues(state, state.getCaster(), time);
        }
        return true;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object o) {
        //半场距离
        final int distance = BattleConstant.MAX_X / 2;
        final boolean attacker = unit.getFriend().isAttacker();
        final Map<String, Object> addition = state.getAddition(Map.class);
        Boolean isAdd = (Boolean) addition.getOrDefault(ADD_FLAG, Boolean.FALSE);
        //是否处于地方半径
        final boolean inTargetPoint = inTargetPoint(distance, unit, attacker);
        if (inTargetPoint && !isAdd) {
            addValues(state, unit, time);
        } else if (!inTargetPoint && isAdd) {
            removeValues(state, unit, time);
        }
    }

    @Override
    public void remove(BuffState state, Unit unit, int time) {
        unit.removeBuff(state);
        removeValues(state, unit, time);
    }

    private void addValues(BuffState state, Unit unit, int time) {
        final DefaultAddValueParam param = state.getParam(DefaultAddValueParam.class);
        final Map<String, Object> addition = state.getAddition(Map.class);
        CalValues calValues = (CalValues) addition.get(ADD_VALUES);
        if (calValues == null) {
            calValues = CalTypeHelper.calValues(param.getCalType(), param.getAlters(), state.getCaster(), unit, param.getFactor());
            addition.put(ADD_VALUES, calValues);
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
        addition.put(ADD_FLAG, Boolean.TRUE);
    }

    private void removeValues(BuffState state, Unit unit, int time) {
        final Map<String, Object> addition = state.getAddition(Map.class);
        CalValues calValues = (CalValues) addition.get(ADD_VALUES);
        if (calValues == null) {
            return;
        }
        Map<AlterType, Number> values = calValues.getValues();
        BuffReport buffReport = state.getBuffReport();
        Context context = new Context(state.getCaster());
        for (Map.Entry<AlterType, Number> entry : values.entrySet()) {
            AlterType alterType = entry.getKey();
            final Alter alter = alterType.getAlter();
            Number number = alter.getReverse(entry.getValue());
            context.addValue(unit, alterType, number);
            buffReport.add(time, unit.getId(), new UnitValues(alterType, number));
        }
        context.execute(time, buffReport);
        addition.put(ADD_FLAG, Boolean.FALSE);
    }

    private boolean inTargetPoint(int distance, Unit unit, boolean attacker) {
        final int x = unit.getPoint().getX();
        if (x > distance && attacker) {
            return true;
        }
        if (x < distance && !attacker) {
            return true;
        }
        return false;
    }
}
