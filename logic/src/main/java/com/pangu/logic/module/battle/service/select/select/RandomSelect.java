package com.pangu.logic.module.battle.service.select.select;

import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.Collections;
import java.util.List;

public class RandomSelect implements Selector {
    @Override
    public List<Unit> select(Unit owner, List<Unit> units, SelectSetting selectSetting, int time) {
        final int amount = selectSetting.getCount();
        if (units.size() <= amount) {
            return units;
        }
        Collections.shuffle(units);
        return units.subList(0, amount);
    }
}
