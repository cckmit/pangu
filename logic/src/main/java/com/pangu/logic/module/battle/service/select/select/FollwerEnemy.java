package com.pangu.logic.module.battle.service.select.select;

import com.pangu.logic.module.battle.service.select.select.utils.Circle;
import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FollwerEnemy implements Selector {
    @Override
    public List<Unit> select(Unit owner, List<Unit> units, SelectSetting selectSetting, int time) {
        Unit traceUnit = owner.getTraceUnit();
        if (!owner.hasState(UnitState.FOLLOW, time) || traceUnit == null || traceUnit.isDead()) {
            return Collections.emptyList();
        }
        Point point = traceUnit.getPoint();
        Circle circle = new Circle(point.getX(), point.getY(), selectSetting.getWidth());
        List<Unit> valid = new ArrayList<>(units.size());
        for (Unit unit : units) {
            Point pos = unit.getPoint();
            if (circle.inShape(pos.getX(), pos.getY())) {
                valid.add(unit);
            }
        }
        return valid;
    }
}
