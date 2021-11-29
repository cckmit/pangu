package com.pangu.logic.module.battle.model;

import com.pangu.framework.protocol.annotation.Transable;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 修改后的属性值
 */
@Transable
@Data
public class AlterAfterValue {

    /**
     * 变化后的值
     */
    private Map<String, Long> values;

    /**
     * 变化后的属性值
     */
    private Map<String, Double> rates;

    public void put(AlterAfterValue value) {
        if (value.values != null) {
            if (this.values == null) {
                this.values = value.values;
            } else {
                this.values.putAll(value.values);
            }
        }
        if (value.rates != null) {
            if (this.rates == null) {
                this.rates = value.rates;
            } else {
                this.rates.putAll(value.rates);
            }
        }
    }

    public void put(UnitValue unitValue, long current) {
        if (values == null) {
            values = new HashMap<>(4);
        }
        values.put(unitValue.name(), current);
    }

    public void put(UnitRate rate, double current) {
        if (rates == null) {
            this.rates = new HashMap<>(4);
        }
        this.rates.put(rate.name(), current);
    }

    public Long getValue(UnitValue type) {
        if (values == null) {
            return null;
        }
        return values.get(type.name());
    }

    public void merge(AlterAfterValue val) {
        Map<String, Long> values = val.getValues();
        if (values != null) {
            if (this.values == null) {
                this.values = values;
            } else {
                this.values.putAll(values);
            }
        }
        Map<String, Double> rates = val.getRates();
        if (rates != null) {
            if (this.rates == null) {
                this.rates = rates;
            } else {
                this.rates.putAll(rates);
            }
        }

    }
}
