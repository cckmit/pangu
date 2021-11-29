package com.pangu.logic.module.battle.service.alter;

import com.pangu.logic.module.battle.model.AlterAfterValue;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.service.core.Unit;

/**
 * 更新护盾值
 */
public class ShieldUpdateAlter extends IntegerTemplate {
    @Override
    public void execute(Unit unit, Number pre, AlterAfterValue alterAfter, int time) {
        long value = pre.longValue();
        if (value > 0) {
            long hpMax = unit.getValue(UnitValue.HP_MAX);
            long curShield = unit.getValue(UnitValue.SHIELD);
            if (hpMax < (curShield + value)) {
                unit.setValue(UnitValue.SHIELD, hpMax);
                alterAfter.put(UnitValue.SHIELD, hpMax);
                return;
            }
        }
        long after = unit.increaseValue(UnitValue.SHIELD, value);
        alterAfter.put(UnitValue.SHIELD, after);
    }
}
