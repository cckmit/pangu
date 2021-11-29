package com.pangu.logic.module.battle.service.alter;


import com.pangu.logic.module.battle.model.AlterAfterValue;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.service.core.Unit;

/**
 * 同时修改{@link UnitValue#HP}与{@link UnitValue#HP_MAX}
 */
public class LifeAlter extends IntegerTemplate {

    @Override
    public void execute(Unit unit, Number pre, AlterAfterValue alterAfter, int time) {
        long value = pre.longValue();
        long curMax = unit.increaseValue(UnitValue.HP_MAX, value);
        alterAfter.put(UnitValue.HP_MAX, curMax);
        long curHp = unit.increaseValue(UnitValue.HP, value);
        alterAfter.put(UnitValue.HP, curHp);
    }

}
