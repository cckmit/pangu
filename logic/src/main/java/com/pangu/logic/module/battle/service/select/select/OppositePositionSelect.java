package com.pangu.logic.module.battle.service.select.select;

import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class OppositePositionSelect implements Selector {
    @Override
    public List<Unit> select(Unit owner, List<Unit> units, SelectSetting selectSetting, int time) {
        if (units.isEmpty()) {
            return Collections.emptyList();
        }
        Point point = owner.getPoint();
        int x = point.getX();
        int y = point.getY();
        int half = BattleConstant.MAX_X / 2;
        int targetX = half + half - x;

        Point circlePoint = new Point(targetX, y);
        final Unit nearest = units.stream()
                .sorted(Comparator.comparingInt(unit -> unit.getPoint().distance(circlePoint)))
                .collect(Collectors.toList()).get(0);

        return Collections.singletonList(nearest);
    }
}
