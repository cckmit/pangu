package com.pangu.logic.module.battle.service.buff.utils;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.CalType;
import com.pangu.logic.module.battle.model.UnitRate;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.service.alter.Alter;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.ctx.OwnerTargetCtx;
import com.pangu.logic.module.common.resource.Formula;
import com.pangu.logic.utils.ExpressionHelper;

import java.util.HashMap;
import java.util.Map;

public class CalTypeHelper {

    public static CalValues calValues(CalType calType, Map<AlterType, String> values, Unit owner, Unit target,
                                      double factor) {
        OwnerTargetCtx ctx = new OwnerTargetCtx(0, owner, target, factor);
        Map<AlterType, Number> alterValues = new HashMap<>(values.size());
        for (Map.Entry<AlterType, String> e : values.entrySet()) {
            AlterType alterType = e.getKey();
            Alter alter = alterType.getAlter();
            String string = e.getValue();
            Number value = null;
            switch (calType) {
            case VALUE:
                value = alter.toValue(string);
                break;
            case FORMULA:
                Formula formula = BuffFactory.getFormula(string);
                value = (Number) formula.calculate(ctx);
                break;
            case EXP:
                value = ExpressionHelper.invoke(string, Number.class, ctx);
                break;
            }
            alterValues.put(alterType, value);
        }
        return new CalValues(alterValues);
    }

    public static Map<UnitValue, Long> calUnitValues(CalType calType, Map<UnitValue, String> values, Unit owner,
                                                     Unit target, double factor, Object customCtx) {
        Object ctx = customCtx;
        if (ctx == null) {
            ctx = new OwnerTargetCtx(0, owner, target, factor);
        }
        Map<UnitValue, Long> result = new HashMap<>(values.size());
        for (Map.Entry<UnitValue, String> e : values.entrySet()) {
            UnitValue unitValue = e.getKey();
            String string = e.getValue();
            Long value = null;
            switch (calType) {
            case VALUE:
                value = Long.valueOf(string);
                break;
            case FORMULA:
                Formula formula = BuffFactory.getFormula(string);
                value = ((Number) formula.calculate(ctx)).longValue();
                break;
            case EXP:
                value = ExpressionHelper.invoke(string, Long.class, ctx);
                break;
            }
            result.put(unitValue, value);
        }
        return result;
    }

    public static Map<UnitRate, Double> calUnitRates(CalType calType, Map<UnitRate, String> rates, Unit owner,
                                                     Unit target, double factor, Object customCtx) {
        Object ctx = customCtx;
        if (ctx == null) {
            ctx = new OwnerTargetCtx(0, owner, target, factor);
        }
        Map<UnitRate, Double> result = new HashMap<>(rates.size());
        for (Map.Entry<UnitRate, String> e : rates.entrySet()) {
            UnitRate unitRate = e.getKey();
            String string = e.getValue();
            Double value = null;
            switch (calType) {
            case VALUE:
                value = Double.valueOf(string);
                break;
            case FORMULA:
                Formula formula = BuffFactory.getFormula(string);
                value = ((Number) formula.calculate(ctx)).doubleValue();
                break;
            case EXP:
                value = ExpressionHelper.invoke(string, Double.class, ctx);
                break;
            }
            result.put(unitRate, value);
        }
        return result;
    }
}
