package com.pangu.logic.module.battle.service.alter;

import com.pangu.logic.module.battle.model.AlterAfterValue;
import com.pangu.logic.module.battle.model.UnitRate;
import com.pangu.logic.module.battle.service.core.Unit;

/**
 * 由于物免率和魔免率总是同时被修改相同的数值，故定义该修改器统一修改
 */
public class UnHarmAlter extends DoubleTemplate{
    @Override
    public void execute(Unit unit, Number value, AlterAfterValue alterAfter, int time) {
        double curUnHarmPRate = unit.increaseRate(UnitRate.UNHARM_P, value.doubleValue());
        double curUnHarmMRate = unit.increaseRate(UnitRate.UNHARM_M, value.doubleValue());

        alterAfter.put(UnitRate.UNHARM_P, curUnHarmMRate);
        alterAfter.put(UnitRate.UNHARM_M, curUnHarmPRate);
    }
}
