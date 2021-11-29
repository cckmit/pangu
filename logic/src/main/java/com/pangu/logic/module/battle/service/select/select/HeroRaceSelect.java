package com.pangu.logic.module.battle.service.select.select;

import com.pangu.logic.module.battle.model.HeroRaceType;
import com.pangu.logic.module.battle.model.ModelInfo;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.core.Unit;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class HeroRaceSelect implements Selector {
    @Override
    public List<Unit> select(Unit owner, List<Unit> units, SelectSetting selectSetting, int time) {
        if (CollectionUtils.isEmpty(units)) {
            return null;
        }
        final HeroRaceType realParam = selectSetting.getRealParam(HeroRaceType.class);
        List<Unit> list = new ArrayList<>(units.size());
        for (Unit unit : units) {
            final ModelInfo model = unit.getModel();
            if (model == null) {
                continue;
            }
            if (model.getRaceType() == realParam) {
                list.add(unit);
            }
        }
        return list;
    }
}
