package com.pangu.logic.module.battle.service.select.sort;

import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.Collections;
import java.util.List;

public class Random implements SortProcessor{
    @Override
    public List<Unit> sort(Point position, List<Unit> units) {
        Collections.shuffle(units);
        return units;
    }
}
