package com.pangu.logic.module.battle.service.select.sort;

import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.Comparator;
import java.util.List;

/**
 * 物理防御力由高至低
 */
public class DEF_P_LOW implements SortProcessor {
    @Override
    public List<Unit> sort(Point position, List<Unit> units) {
        units.sort(Comparator.comparingLong(a -> a.getValue(UnitValue.DEFENCE_P)));
        return units;
    }
}
