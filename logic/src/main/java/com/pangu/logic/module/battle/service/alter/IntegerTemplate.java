package com.pangu.logic.module.battle.service.alter;

public abstract class IntegerTemplate implements Alter {

    @Override
    public Long add(Number current, Number value) {
        if (current == null) {
            return value.longValue();
        } else {
            return current.longValue() + value.longValue();
        }
    }

    @Override
    public Long getReverse(Number n) {
        return -n.longValue();
    }

    @Override
    public Long toValue(String value) {
        return Double.valueOf(value).longValue();
    }
}
