package com.pangu.logic.module.battle.service.select.filter;

import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.List;
import java.util.stream.Collectors;

public class EnemyFilter implements Filter {
    @Override
    public List<Unit> filter(Unit unit, int time) {
        if (unit.hasState(UnitState.CHAOS, time)) {
            return unit.getFriend().getCurrent().stream()
                    .filter(friend -> !friend.hasState(UnitState.CHAOS, time))
                    .collect(Collectors.toList());
        }
        return unit.getEnemy().getCurrent();
    }
}
