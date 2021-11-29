package com.pangu.logic.module.battle.service.select.select;

import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.ArrayList;
import java.util.List;

/**
 * 选择出所有普攻距离大于指定距离的单元，常用于筛选远程单元
 */
public class NormalAtkDistanceMoreThan implements Selector {
    @Override
    public List<Unit> select(Unit owner, List<Unit> units, SelectSetting selectSetting, int time) {
        final Integer threshold = selectSetting.getRealParam(Integer.class);

        final ArrayList<Unit> selected = new ArrayList<>(6);
        for (Unit unit : units) {
            if (unit.getMinMoveDistance() > threshold) {
                selected.add(unit);
            }
        }
        return selected;
    }
}
