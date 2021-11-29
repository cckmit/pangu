package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.DefaultAddValueParam;
import com.pangu.logic.module.battle.service.buff.param.ValDrainParam;
import com.pangu.logic.module.battle.service.buff.utils.CalTypeHelper;
import com.pangu.logic.module.battle.service.buff.utils.CalValues;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 被星光逆流攻击到的敌人，被攻击的人再接下来的3秒内将其身上能量的20%都将转移给占星魔偶
 */
@Component
public class ValDrain implements Buff {
    @Override
    public BuffType getType() {
        return BuffType.VAL_DRAIN;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object addition) {
        final ValDrainParam param = state.getParam(ValDrainParam.class);
        final DefaultAddValueParam drainedVal = param.getDrainedVal();
        final DefaultAddValueParam drainingVal = param.getDrainingVal();
        final Unit caster = state.getCaster();
        CalValues drainedValues = CalTypeHelper.calValues(drainedVal.getCalType(), drainedVal.getAlters(), caster, unit, drainedVal.getFactor());
        final CalValues drainingValues = CalTypeHelper.calValues(drainingVal.getCalType(), drainingVal.getAlters(), caster, unit, drainingVal.getFactor());
        Context context = new Context(caster);
        final BuffReport buffReport = state.getBuffReport();
        for (Map.Entry<AlterType, Number> entry : drainedValues.getValues().entrySet()) {
            AlterType alterType = entry.getKey();
            Number number = entry.getValue();
            context.addValue(unit, alterType, number);
            buffReport.add(time, unit.getId(), new UnitValues(alterType, number));
        }
        for (Map.Entry<AlterType, Number> entry : drainingValues.getValues().entrySet()) {
            AlterType alterType = entry.getKey();
            Number number = entry.getValue();
            context.addValue(caster, alterType, number);
            buffReport.add(time, caster.getId(), new UnitValues(alterType, number));
        }

        context.execute(time, buffReport);
    }
}
