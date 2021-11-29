package com.pangu.logic.module.battle.service.select.filter;

import com.pangu.logic.module.battle.service.core.Unit;

import java.util.Collections;
import java.util.List;

public class NoUnit implements Filter {
    @Override
    public List<Unit> filter(Unit unit, int time) {
        return Collections.emptyList();
    }
}
