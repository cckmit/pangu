package com.pangu.logic.module.battle.service.alter;

public abstract class DoubleTemplate implements Alter {
    @Override
    public Number add(Number current, Number value) {
        if (current == null) {
            return value;
        } else {
            return current.doubleValue() + value.doubleValue();
        }
    }

    @Override
    public Number getReverse(Number n) {
        return -n.doubleValue();
    }

    @Override
    public Double toValue(String value) {
        return Double.valueOf(value);
    }
}
