package com.pangu.logic.module.battle.service.alter;

import com.pangu.logic.module.battle.model.AlterAfterValue;
import com.pangu.logic.module.battle.model.UnitRate;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.service.core.Unit;

/**
 * 怒气修改器
 */
public class MpAlter extends IntegerTemplate {
    @Override
    public void execute(Unit unit, Number pre, AlterAfterValue alterAfter, int time) {
        long value = pre.longValue();
        value = cal(unit, value);
        if (value == 0) return;
        long mpMax = unit.getValue(UnitValue.MP_MAX);
        long curMp = unit.getValue(UnitValue.MP);
        long afterMp = Math.max(0, curMp + value);
        if (afterMp > mpMax) {
            unit.setValue(UnitValue.MP, mpMax);
            alterAfter.put(UnitValue.MP, mpMax);
        } else {
            unit.setValue(UnitValue.MP, afterMp);
            alterAfter.put(UnitValue.MP, afterMp);
        }
    }

    protected long cal(Unit unit, long value){
        return calMpChange(unit, value);
    }

    public static long calMpChange(Unit unit, long value) {
        if (value > 0) {
            value = (long) (value * (1 + unit.getRate(UnitRate.MP_ADD_RATE)));
            value = Math.max(0, value);
        }
        return value;
    }
}
