package com.pangu.logic.module.battle.service.select.select;

import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.select.utils.BestRectangle;

import java.util.Collections;
import java.util.List;

public class BestRectangleSelect implements Selector{
    @Override
    public List<Unit> select(Unit owner, List<Unit> units, SelectSetting selectSetting, int time) {
        final int width = selectSetting.getWidth();
        final int length = selectSetting.getDistance();
        final BestRectangle.UnitInfo info = BestRectangle.calBestRectangle(owner, units, width, length);
        if (info == null) {
            return Collections.emptyList();
        }
        return info.getInRect();
    }
}
