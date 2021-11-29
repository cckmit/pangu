package com.pangu.logic.module.battle.service.select.filter;


import com.pangu.logic.module.battle.service.core.Unit;

import java.util.List;

public class AliveFriend implements Filter {
    @Override
    public List<Unit> filter(Unit unit, int time) {
        return unit.getFriend().getCurrent();
    }

    @Override
    public List<Unit> list(Unit unit, int time) {
        return filter(unit, time);
    }
}
