package com.pangu.logic.module.battle.service.select.select;

import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.select.utils.Circle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TargetCircleNoTarget implements Selector {
    @Override
    public List<Unit> select(Unit owner, List<Unit> units, SelectSetting selectSetting, int time) {
        Unit target = owner.getTarget();
        if (target == null) {
            return Collections.emptyList();
        }
        Point point = target.getPoint();
        Circle circle = new Circle(point.getX(), point.getY(), selectSetting.getWidth());
        List<Unit> valid = new ArrayList<>(units.size());
        for (Unit unit : units) {
            if (unit == target) {
                continue;
            }
            Point pos = unit.getPoint();
            if (circle.inShape(pos.getX(), pos.getY())) {
                valid.add(unit);
            }
        }
        return valid;
    }
}