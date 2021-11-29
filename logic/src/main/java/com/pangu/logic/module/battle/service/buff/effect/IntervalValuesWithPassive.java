package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.IntervalValuesWithPassiveParam;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveFactory;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 附带被动的定时属性修改器
 */
@Component
public class IntervalValuesWithPassive extends IntervalChangeValues {
    @Override
    public BuffType getType() {
        return BuffType.INTERVAL_VALUES_WITH_PASSIVE;
    }

    @Override
    public boolean add(BuffState state, Unit unit, int time) {
        String passiveId = state.getParam(IntervalValuesWithPassiveParam.class).getPassive();
        if (StringUtils.isEmpty(passiveId)) {
            return true;
        }
        PassiveState passiveState = PassiveFactory.initState(passiveId, time);
        unit.addPassive(passiveState, state.getCaster());
        return true;
    }

    @Override
    public void remove(BuffState state, Unit unit, int time) {
        String passiveId = state.getParam(IntervalValuesWithPassiveParam.class).getPassive();
        if (StringUtils.isEmpty(passiveId)) {
            return;
        }
        PassiveState passiveState = unit.getPassiveStates(passiveId);
        if (passiveState == null) {
            return;
        }
        unit.removePassive(passiveState);
    }
}
