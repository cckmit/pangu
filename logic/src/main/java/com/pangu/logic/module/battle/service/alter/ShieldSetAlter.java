package com.pangu.logic.module.battle.service.alter;

import com.pangu.logic.module.battle.model.AlterAfterValue;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.service.core.Unit;

/**
 * 设置护盾值，只有初始设置时才会如此
 */
public class ShieldSetAlter extends IntegerTemplate {
    @Override
    public void execute(Unit unit, Number pre, AlterAfterValue alterAfter, int time) {
        long value = pre.longValue();
        if (value > 0) {
            long preShield = unit.getValue(UnitValue.SHIELD);
            if (preShield < value) {
                value = Math.min(unit.getValue(UnitValue.HP_MAX), value);
                unit.setValue(UnitValue.SHIELD, value);
                alterAfter.put(UnitValue.SHIELD, value);
                return;
            }
        }
        final long after = unit.increaseValue(UnitValue.SHIELD, value);
        alterAfter.put(UnitValue.SHIELD, after);
    }
}
