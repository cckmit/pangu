package com.pangu.logic.module.battle.service.select.select;

import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.filter.utils.AreaFilter;
import com.pangu.logic.module.battle.service.skill.param.AreaParam;

import java.util.ArrayList;
import java.util.List;

public class AreaSelect implements Selector {

    @Override
    public List<Unit> select(Unit owner, List<Unit> units, SelectSetting selectSetting, int time) {
        final ArrayList<Unit> targets = new ArrayList<>(units.size());
        final AreaParam areaParam = selectSetting.getRealParam(AreaParam.class);
        targets.addAll(AreaFilter.filterUnitInArea(units, areaParam));
        return targets;
    }
}
