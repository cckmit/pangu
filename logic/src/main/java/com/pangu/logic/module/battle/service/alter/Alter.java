package com.pangu.logic.module.battle.service.alter;


import com.pangu.logic.module.battle.model.AlterAfterValue;
import com.pangu.logic.module.battle.service.core.Unit;

public interface Alter {

    /**
     * 执行数值变更
     */
    void execute(Unit unit, Number value, AlterAfterValue alterAfter, int time);

    /**
     * 添加变更数值
     *
     * @param current
     * @param value
     * @return
     */
    Number add(Number current, Number value);

    /**
     * 获取指定值的方向数值
     */
    Number getReverse(Number n);

    /**
     * 将字符串表现的值转为对象形式
     *
     * @param value
     * @return
     */
    Number toValue(String value);
}
