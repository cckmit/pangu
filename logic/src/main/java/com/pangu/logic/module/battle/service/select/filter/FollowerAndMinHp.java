package com.pangu.logic.module.battle.service.select.filter;

import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 跟随者以及血量最低的单元
 */
public class FollowerAndMinHp implements Filter {
    @Override
    public List<Unit> filter(Unit unit, int time) {
        return Collections.emptyList();
    }

    @Override
    public List<Unit> list(Unit unit, int time) {
        List<Unit> current = unit.getFriend().getCurrent();
        if (current.size() == 1) {
            return Collections.emptyList();
        }
        List<Unit> targets = null;
        Unit traceUnit = unit.getTraceUnit();
        if (unit.hasState(UnitState.FOLLOW, time) && traceUnit != null && !traceUnit.isDead()) {
            targets = new ArrayList<>(2);
            targets.add(traceUnit);
        }
        long minHp = Long.MAX_VALUE;
        Unit minHpUnit = null;
        for (Unit unitItem : current) {
//            if (unitItem == unit) {
//                continue;
//            }
            if (unitItem.isDead() || unitItem.hasState(UnitState.UNVISUAL, time) || unitItem.hasState(UnitState.EXILE, time)) {
                continue;
            }
            long hp = unitItem.getValue(UnitValue.HP);
            if (hp > 0 && hp < minHp) {
                minHp = hp;
                minHpUnit = unitItem;
            }
        }
        if (minHpUnit != null) {
            if (targets == null) {
                return Collections.singletonList(minHpUnit);
            }
            targets.add(minHpUnit);
            return targets;
        }
        if (targets == null) {
            return Collections.emptyList();
        }
        return targets;
    }
}
