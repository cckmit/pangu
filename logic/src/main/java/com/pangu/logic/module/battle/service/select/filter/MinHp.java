package com.pangu.logic.module.battle.service.select.filter;

import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.Comparator;
import java.util.List;

public class MinHp implements Filter {
    @Override
    public List<Unit> filter(Unit unit, int time) {
        List<Unit> allLive = unit.getEnemy().getCurrent();
        allLive.sort(Comparator.comparingLong(a -> a.getValue(UnitValue.HP)));
        return allLive;
    }
}
