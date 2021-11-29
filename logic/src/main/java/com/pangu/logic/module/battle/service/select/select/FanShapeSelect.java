package com.pangu.logic.module.battle.service.select.select;

import com.pangu.logic.module.battle.service.select.select.utils.FanShaped;
import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FanShapeSelect implements Selector {
    @Override
    public List<Unit> select(Unit owner, List<Unit> units, SelectSetting selectSetting, int time) {
        Unit target = owner.getTarget();
        if (target == null) {
            return Collections.emptyList();
        }
        FanShaped fanShaped = new FanShaped(owner.getPoint(), target.getPoint(), selectSetting.getWidth(), selectSetting.getDistance());
        List<Unit> valid = new ArrayList<>(units.size());
        for (Unit unit : units) {
            Point pos = unit.getPoint();
            if (fanShaped.inShape(pos.getX(), pos.getY())) {
                valid.add(unit);
            }
        }
        return valid;
    }
}
