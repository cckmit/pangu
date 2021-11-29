package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.NoEnemyAroundAddValuesParam;
import com.pangu.logic.module.battle.service.buff.utils.CalTypeHelper;
import com.pangu.logic.module.battle.service.buff.utils.CalValues;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 周围没有敌方英雄添加属性
 */
@Component
public class NoEnemyAroundAddValues implements Buff {
    //添加的属性
    public static final String VALUES = "values";
    //是否已添加过
    public static final String ADD_FLAG = "flag";

    @Override
    public BuffType getType() {
        return BuffType.NO_ENEMY_AROUND_ADD_VALUES;
    }

    @Override
    public boolean add(BuffState state, Unit unit, int time) {
        state.setAddition(new HashMap<String, Object>());
        unit.addBuff(state);
        update(state, unit, time, null);
        return true;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object o) {
        final NoEnemyAroundAddValuesParam param = state.getParam(NoEnemyAroundAddValuesParam.class);
        final boolean hasEnemyAroundMe = enemyAround(unit, param.getDistance());
        Map<String, Object> addition = state.getAddition(Map.class);
        Boolean add = (Boolean) addition.getOrDefault(ADD_FLAG, Boolean.FALSE);
        final CalValues calValues = (CalValues) addition.get(VALUES);
        if (hasEnemyAroundMe && add) {
            removeValues(unit, state, calValues, time);
        } else if (!hasEnemyAroundMe && !add) {
            addValues(unit, time, state, param);
        }

    }

    private boolean enemyAround(Unit unit, int distance) {
        for (Unit enemy : unit.getEnemy().getCurrent()) {
            if (enemy.getPoint().distance(unit.getPoint()) < distance) {
                return false;
            }
        }
        return true;
    }

    private void removeValues(Unit unit, BuffState state, CalValues calValues, int time) {
        if (calValues == null) {
            return;
        }
        BuffReport buffReport = state.getBuffReport();
        Context context = new Context(state.getCaster());
        for (Map.Entry<AlterType, Number> entry : calValues.getValues().entrySet()) {
            AlterType alterType = entry.getKey();
            final Number reverse = alterType.getAlter().getReverse(entry.getValue());
            context.addValue(unit, alterType, reverse);
            buffReport.add(time, unit.getId(), new UnitValues(alterType, reverse));
        }
        context.execute(time, buffReport);
    }

    private void addValues(Unit unit, int time, BuffState state, NoEnemyAroundAddValuesParam param) {
        Map<String, Object> addition = state.getAddition(Map.class);
        final CalValues calValues = CalTypeHelper.calValues(param.getCalType(), param.getAlters(), unit, null, param.getFactor());
        BuffReport buffReport = state.getBuffReport();
        Context context = new Context(state.getCaster());
        for (Map.Entry<AlterType, Number> entry : calValues.getValues().entrySet()) {
            AlterType alterType = entry.getKey();
            Number number = entry.getValue();
            context.addValue(unit, alterType, number);
            buffReport.add(time, unit.getId(), new UnitValues(alterType, number));
        }
        context.execute(time, buffReport);
        addition.put(VALUES, calValues);
        addition.put(ADD_FLAG, Boolean.TRUE);
    }

    @Override
    public void remove(BuffState state, Unit unit, int time) {
        unit.removeBuff(state);
        Map<String, Object> addition = state.getAddition(Map.class);
        final CalValues calValues = (CalValues) addition.get(VALUES);
        removeValues(unit, state, calValues, time);
    }
}
