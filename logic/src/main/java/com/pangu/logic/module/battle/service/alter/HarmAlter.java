package com.pangu.logic.module.battle.service.alter;

import com.pangu.logic.module.battle.model.AlterAfterValue;
import com.pangu.logic.module.battle.model.UnitRate;
import com.pangu.logic.module.battle.service.core.Unit;

/**
 * 由于物伤率和魔伤率总是同时被修改相同的数值，故定义该修改器统一修改
 */
public class HarmAlter extends DoubleTemplate {
    @Override
    public void execute(Unit unit, Number value, AlterAfterValue alterAfter, int time) {
        double curHarmPRate = unit.increaseRate(UnitRate.HARM_P, value.doubleValue());
        double curHarmMRate = unit.increaseRate(UnitRate.HARM_M, value.doubleValue());

        alterAfter.put(UnitRate.HARM_P, curHarmPRate);
        alterAfter.put(UnitRate.HARM_M, curHarmMRate);
    }
}
