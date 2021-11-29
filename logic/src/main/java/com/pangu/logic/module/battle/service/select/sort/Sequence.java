package com.pangu.logic.module.battle.service.select.sort;

import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Sequence implements SortProcessor{
    @Override
    public List<Unit> sort(Point position, List<Unit> units) {
        return units.stream()
                .sorted(Comparator.comparingInt(Unit::getSequence))
                .collect(Collectors.toList());
    }
}
