package com.pangu.logic.module.battle.service.select.select;

import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.ArrayList;
import java.util.List;

public class HeroSelect implements Selector {
    @Override
    public List<Unit> select(Unit owner, List<Unit> units, SelectSetting selectSetting, int time) {
        final ArrayList<Unit> selected = new ArrayList<>();
        for (Unit unit : units) {
            if (unit.heroUnit()) {
                selected.add(unit);
            }
        }
        return selected;
    }
}
