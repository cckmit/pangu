package com.pangu.logic.module.battle.service.select.filter;

import com.pangu.logic.module.battle.service.core.Unit;

import java.util.ArrayList;
import java.util.List;

public class AllFilter implements Filter {
    @Override
    public List<Unit> filter(Unit unit, int time) {
        List<Unit> friend = unit.getFriend().getCurrent();
        List<Unit> enemy = unit.getEnemy().getCurrent();
        List<Unit> all = new ArrayList<>(friend.size() + enemy.size());
        all.addAll(friend);
        all.addAll(enemy);
        return all;
    }
}
