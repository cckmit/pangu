package com.pangu.logic.module.battle.service.select.sort;

import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 按战力从高到低进行排序
 */
public class FightHight implements SortProcessor{
    @Override
    public List<Unit> sort(Point position, List<Unit> units) {
        return units.stream()
                .sorted(Comparator.comparingLong(Unit::getFight).reversed())
                .collect(Collectors.toList());
    }
}
