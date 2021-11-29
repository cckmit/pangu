package com.pangu.logic.module.battle.service.select.select;

import com.pangu.logic.module.battle.model.UnitType;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UnitTypeSelect implements Selector {
    @Override
    public List<Unit> select(Unit owner, List<Unit> units, SelectSetting selectSetting, int time) {
        final UnitType realParam = selectSetting.getRealParam(UnitType.class);
        List<Unit> result = new ArrayList<>(units.size());
        for (Unit unit : units) {
            if (unit.getModel() == null) {
                continue;
            }
            Collection<UnitType> professions = unit.getModel().getProfessions();
            if (professions == null) {
                continue;
            }
            if (professions.contains(realParam)) {
                result.add(unit);
            }
        }
        return result;
    }
}
