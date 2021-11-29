package com.pangu.logic.module.battle.service.select.select;

import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.select.utils.Circle;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
@Component
public class RandomTargetCircle implements Selector{
    @Override
    public List<Unit> select(Unit owner, List<Unit> units, SelectSetting selectSetting, int time) {
        Collections.shuffle(units);
        final Unit target = units.get(0);
        Circle circle = new Circle(target.getPoint().getX(), target.getPoint().getY(), selectSetting.getWidth());
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
