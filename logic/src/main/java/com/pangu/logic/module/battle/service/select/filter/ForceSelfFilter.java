package com.pangu.logic.module.battle.service.select.filter;

import com.pangu.logic.module.battle.service.core.Unit;

import java.util.Collections;
import java.util.List;

public class ForceSelfFilter implements Filter {

    @Override
    public List<Unit> filter(Unit unit, int time) {
        return Collections.singletonList(unit);
    }

    @Override
    public List<Unit> list(Unit unit, int time) {
        List<Unit> all = filter(unit, time);
        return all;
    }
}
