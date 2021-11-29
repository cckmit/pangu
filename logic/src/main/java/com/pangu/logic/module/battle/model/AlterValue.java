package com.pangu.logic.module.battle.model;

import com.pangu.logic.module.battle.service.alter.Alter;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.HashMap;
import java.util.Map;

/**
 * 战斗单元的数值变更对象
 */
public class AlterValue {

    // 做属性计算
    private final Map<AlterType, Number> values = new HashMap<>(4);

    /**
     * 获取指定值
     */
    public Number getValue(AlterType type) {
        return values.get(type);
    }

    /**
     * 获取指定值
     */
    public long getLongValue(AlterType type) {
        Number number = values.get(type);
        if (number == null) {
            return 0L;
        }
        return number.longValue();
    }

    /**
     * 获取指定值
     */
    public double getDoubleValue(AlterType type) {
        Number number = values.get(type);
        if (number == null) {
            return 0.0;
        }
        return number.doubleValue();
    }

    /**
     * 添加值
     */
    public void addValue(AlterType type, Number value) {
        Alter alter = type.getAlter();
        Number preValue = values.get(type);
        if (preValue == null) {
            values.put(type, value);
            return;
        }
        value = alter.add(preValue, value);
        values.put(type, value);
    }

    @Override
    public String toString() {
        return values.toString();
    }

    public void addValue(AlterValue alterValue) {
        for (Map.Entry<AlterType, Number> entry : alterValue.values.entrySet()) {
            AlterType type = entry.getKey();
            Number value = entry.getValue();
            addValue(type, value);
        }
    }

    public void execute(Unit unit, AlterAfterValue afterValue, int time) {
        for (Map.Entry<AlterType, Number> entry : values.entrySet()) {
            AlterType alterType = entry.getKey();
            Number number = entry.getValue();
            Alter alter = alterType.getAlter();
            alter.execute(unit, number, afterValue, time);
        }
    }
}
