package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.buff.param.DefaultAddValueParam;
import com.pangu.logic.module.battle.service.buff.utils.CalTypeHelper;
import com.pangu.logic.module.battle.service.buff.utils.CalValues;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitDiePassive;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class FriendDieAddValues implements UnitDiePassive {
    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> dieUnits) {
        final DefaultAddValueParam param = passiveState.getParam(DefaultAddValueParam.class);
        boolean add = false;
        for (Unit dieUnit : dieUnits) {
            if (dieUnit.getFriend() == owner.getFriend()) {
                final CalValues calValues = CalTypeHelper.calValues(param.getCalType(), param.getAlters(), owner, dieUnit, param.getFactor());
                for (Map.Entry<AlterType, Number> entry : calValues.getValues().entrySet()) {
                    AlterType alterType = entry.getKey();
                    Number number = entry.getValue();
                    damageReport.add(time, owner.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), new UnitValues(alterType, number)));
                    context.addPassiveValue(owner, entry.getKey(), entry.getValue());
                }
                add = true;
            }
        }
        if (add) {
            passiveState.addCD(time);
        }
    }

    @Override
    public PassiveType getType() {
        return PassiveType.FRIEND_DIE_ADD_VALUES;
    }
}
