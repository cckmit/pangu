package com.pangu.logic.module.battle.service.select.select;

import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.core.Fighter;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 目标周围的地方单元
 */
public class AroundEnemy implements Selector {
    @Override
    public List<Unit> select(Unit owner, List<Unit> units, SelectSetting selectSetting, int time) {
        if (units == null || units.isEmpty()) {
            return Collections.emptyList();
        }
        List<Unit> result = null;
        int distance = selectSetting.getWidth();
        for (Unit unit : units) {
            Point fromPoint = unit.getPoint();
            Fighter enemyFighter = unit.getEnemy();
            List<Unit> current = enemyFighter.getCurrent();
            for (Unit item : current) {
                if (item.isDead() || item.hasState(UnitState.UNVISUAL, time) || item.hasState(UnitState.EXILE, time)) {
                    continue;
                }
                if (result == null) {
                    result = new ArrayList<>(6);
                }
                if (item.getPoint().distance(fromPoint) <= distance) {
                    result.add(item);
                }
            }
        }
        if (result == null) {
            return Collections.emptyList();
        }
        return result;
    }
}
