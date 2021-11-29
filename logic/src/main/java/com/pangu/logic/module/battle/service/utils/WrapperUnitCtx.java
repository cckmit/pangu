package com.pangu.logic.module.battle.service.utils;

import com.pangu.logic.module.battle.service.core.Unit;

import java.util.Collections;
import java.util.Map;

public class WrapperUnitCtx {

    private final Unit unit;

    private Map<String, Long> valuesAdd;

    private Map<String, Double> ratesAdd;

    private Map<String, Long> valuesSet;

    private Map<String, Double> ratesSet;

    public WrapperUnitCtx(Unit unit,
                          Map<String, Long> valuesAdd,
                          Map<String, Double> ratesAdd,
                          Map<String, Long> valuesSet,
                          Map<String, Double> ratesSet) {
        this.unit = unit;
        this.valuesAdd = valuesAdd;
        if (this.valuesAdd == null) {
            this.valuesAdd = Collections.emptyMap();
        }
        this.ratesAdd = ratesAdd;
        if (this.ratesAdd == null) {
            this.ratesAdd = Collections.emptyMap();
        }
        this.valuesSet = valuesSet;
        if (this.valuesSet == null) {
            this.valuesSet = Collections.emptyMap();
        }
        this.ratesSet = ratesSet;
        if (this.ratesSet == null) {
            this.ratesSet = Collections.emptyMap();
        }
    }

    public long getValue(String type) {
        Long setValue = valuesSet.get(type);
        if (setValue != null) {
            return setValue;
        }
        Long cache = valuesAdd.get(type);
        if (cache == null) {
            return unit.getValue(type);
        }
        return cache + unit.getValue(type);
    }

    public double getRate(String type) {
        Double setRate = ratesSet.get(type);
        if (setRate != null) {
            return setRate;
        }
        Double cache = ratesAdd.get(type);
        if (cache == null) {
            return unit.getRate(type);
        }
        return cache + unit.getRate(type);
    }
}
