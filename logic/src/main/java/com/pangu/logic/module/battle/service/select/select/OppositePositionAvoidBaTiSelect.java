package com.pangu.logic.module.battle.service.select.select;

import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 优先选取非霸体的对位单元
 */
public class OppositePositionAvoidBaTiSelect implements Selector {
    @Override
    public List<Unit> select(Unit owner, List<Unit> units, SelectSetting selectSetting, int time) {
        if (units.isEmpty()) {
            return Collections.emptyList();
        }

        Point point = owner.getPoint();
        int x = point.getX();
        int y = point.getY();
        int targetX = BattleConstant.MAX_X - x;

        Point circlePoint = new Point(targetX, y);

        final List<Unit> candidatesControllable = units.stream()
                .filter(unit -> !unit.immuneControl(time))
                .sorted(Comparator.comparingInt(unit -> unit.getPoint().distance(circlePoint)))
                .collect(Collectors.toList());

         if (!candidatesControllable.isEmpty()) {
            return Collections.singletonList(candidatesControllable.get(0));
        }

        final List<Unit> candidates = units.stream()
                .sorted(Comparator.comparingInt(unit -> unit.getPoint().distance(circlePoint)))
                .collect(Collectors.toList());
        return Collections.singletonList(candidates.get(0));
    }
}
