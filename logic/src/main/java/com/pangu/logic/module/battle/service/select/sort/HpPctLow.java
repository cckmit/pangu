package com.pangu.logic.module.battle.service.select.sort;

import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.Comparator;
import java.util.List;

/**
 * 血量百分比从低到高
 * author Kubby
 */
public class HpPctLow implements SortProcessor {

    @Override
    public List<Unit> sort(Point position, List<Unit> units) {
        units.sort(Comparator.comparingDouble(a -> a.getHpPct()));
        return units;
    }
}
