package com.pangu.logic.module.battle.service.select.select;

import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.core.Fighter;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class UnitAround implements Selector {

    @Override
    public List<Unit> select(Unit owner, List<Unit> units, SelectSetting selectSetting, int time) {
        if (units == null || units.isEmpty()) {
            return Collections.emptyList();
        }
        List<Unit> result = null;
        int range = selectSetting.getRealParam(int.class);
        for (Unit unit : units) {
            Point fromPoint = unit.getPoint();
            Fighter enemyFighter = unit.getEnemy();
            List<Unit> current = enemyFighter.getCurrent();
            for (Unit enemy : current) {
                if (enemy.isDead() || enemy.hasState(UnitState.UNVISUAL, time) || enemy
                        .hasState(UnitState.EXILE, time)) {
                    continue;
                }
                if (result == null) {
                    result = new LinkedList<>();
                }
                if (result.contains(enemy)) {
                    continue;
                }
                if (enemy.getPoint().distance(fromPoint) <= range) {
                    result.add(enemy);
                }
            }
        }
        if (result == null) {
            return Collections.emptyList();
        }
        return result;
    }
}
