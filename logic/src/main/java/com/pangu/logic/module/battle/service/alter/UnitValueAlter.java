package com.pangu.logic.module.battle.service.alter;

import com.pangu.logic.module.battle.model.AlterAfterValue;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.service.core.Unit;

/**
 * 战斗单位的{@link UnitValue}值修改器
 */
public class UnitValueAlter extends IntegerTemplate {

    //  修改类型
    private final UnitValue type;

    public UnitValueAlter(UnitValue type) {
        this.type = type;
    }

    @Override
    public void execute(Unit unit, Number pre, AlterAfterValue alterAfter, int time) {
        long cur = unit.increaseValue(type, pre.longValue());
        alterAfter.put(type, cur);
    }

    public UnitValue getType() {
        return type;
    }
}
