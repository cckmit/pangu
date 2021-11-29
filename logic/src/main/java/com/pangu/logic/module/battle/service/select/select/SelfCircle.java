package com.pangu.logic.module.battle.service.select.select;

import com.pangu.logic.module.battle.service.select.select.utils.Circle;
import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.ArrayList;
import java.util.List;

/**
 * 以自己为圆心
 */
public class SelfCircle implements Selector {
    @Override
    public List<Unit> select(Unit owner, List<Unit> units, SelectSetting selectSetting, int time) {
        Point point = owner.getPoint();
        Circle circle = new Circle(point.getX(), point.getY(), selectSetting.getDistance());
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
