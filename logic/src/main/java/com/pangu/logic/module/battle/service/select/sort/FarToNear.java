package com.pangu.logic.module.battle.service.select.sort;

import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.service.core.Unit;
import lombok.AllArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FarToNear implements SortProcessor {
    @Override
    public List<Unit> sort(Point position, List<Unit> units) {
        if (CollectionUtils.isEmpty(units)) {
            return units;
        }
        return units.stream()
                .map(unit -> {
                    int distance = position.distance(unit.getPoint());
                    return new Distance.UnitReference(distance, unit);
                })
                .sorted((Comparator.comparingInt(o -> -o.distance)))
                .map(reference -> reference.unit)
                .collect(Collectors.toList());
    }

    @AllArgsConstructor
    static class UnitReference {
        int distance;
        Unit unit;
    }
}
