package com.pangu.logic.module.battle.service.select.select;

import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.List;

/**
 * 取距离最远的N个单位
 */
public class FarthestDistanceSelect implements Selector {
    @Override
    public List<Unit> select(Unit owner, List<Unit> units, SelectSetting selectSetting, int time) {
        final int count = selectSetting.getCount();
        if (units.size() <= count) {
            return units;
        }
        final Point point = owner.getPoint();
        units.sort((o1, o2) -> o2.getPoint().distance(point) - o1.getPoint().distance(point));
        return units.subList(0, count);
    }
}
