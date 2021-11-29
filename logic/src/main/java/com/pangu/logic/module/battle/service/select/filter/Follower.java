package com.pangu.logic.module.battle.service.select.filter;

import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.Collections;
import java.util.List;

public class Follower implements Filter {
    @Override
    public List<Unit> filter(Unit unit, int time) {
        return null;
    }

    @Override
    public List<Unit> list(Unit unit, int time) {
        Unit traceUnit = unit.getTraceUnit();
        if (!unit.hasState(UnitState.FOLLOW, time) || traceUnit == null || traceUnit.isDead()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(traceUnit);
    }
}
