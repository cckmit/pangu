package com.pangu.logic.module.battle.service.select.sort;

import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.service.core.Unit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 按Y轴距离排序
 */
@Component
public class YDistance implements SortProcessor{
    @Override
    public List<Unit> sort(Point position, List<Unit> units) {
        return units.stream()
                .map(unit -> new UnitRef(Math.abs(unit.getPoint().y - position.y), unit))
                .sorted(Comparator.comparingInt(UnitRef::getYDistance))
                .map(UnitRef::getUnit)
                .collect(Collectors.toList());
    }

    @AllArgsConstructor
    @Getter
    private static class UnitRef{
        private int yDistance;
        private Unit unit;
    }
}
