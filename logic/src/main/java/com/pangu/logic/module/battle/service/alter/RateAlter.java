package com.pangu.logic.module.battle.service.alter;

import com.pangu.logic.module.battle.model.AlterAfterValue;
import com.pangu.logic.module.battle.model.UnitRate;
import com.pangu.logic.module.battle.service.core.Unit;

public class RateAlter extends DoubleTemplate {

    private final UnitRate rate;

    public RateAlter(UnitRate rate) {
        this.rate = rate;
    }

    @Override
    public void execute(Unit unit, Number value, AlterAfterValue alterAfter, int time) {
        double current = unit.increaseRate(rate, value.doubleValue());
        alterAfter.put(rate, current);
    }

    public UnitRate getRate() {
        return rate;
    }
}
