package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.buff.utils.CalTypeHelper;
import com.pangu.logic.module.battle.service.buff.utils.CalValues;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.InitPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.InitValuesParam;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InitValues implements InitPassive {
    @Override
    public void init(int time, PassiveState passiveState, Unit owner, Context context, SkillReport skillReport) {
        InitValuesParam param = passiveState.getParam(InitValuesParam.class);
        CalValues calValues = CalTypeHelper.calValues(param.getCalType(), param.getAlters(), owner, owner, param.getFactor());
        Map<AlterType, Number> values = calValues.getValues();
        Number hpChange = values.remove(AlterType.HP);
        String ownerId = owner.getId();
        if (hpChange != null) {
            skillReport.add(time, ownerId, Hp.of(hpChange.longValue()));
            context.addPassiveValue(owner, AlterType.HP, hpChange);
        }
        for (Map.Entry<AlterType, Number> entry : values.entrySet()) {
            AlterType alterType = entry.getKey();
            Number number = entry.getValue();
            skillReport.add(time, ownerId, new UnitValues(alterType, number));
            context.addPassiveValue(owner, alterType, number);
        }
    }

    @Override
    public PassiveType getType() {
        return PassiveType.INIT_VALUES;
    }
}
