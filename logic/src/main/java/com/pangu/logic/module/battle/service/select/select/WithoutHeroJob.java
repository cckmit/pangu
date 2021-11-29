package com.pangu.logic.module.battle.service.select.select;

import com.pangu.logic.module.battle.model.HeroJobType;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.core.Unit;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WithoutHeroJob implements Selector {
    @Override
    public List<Unit> select(Unit owner, List<Unit> units, SelectSetting selectSetting, int time) {
        if (CollectionUtils.isEmpty(units)) {
            return Collections.emptyList();
        }
        final HeroJobType realParam = selectSetting.getRealParam(HeroJobType.class);
        List<Unit> list = new ArrayList<>(units.size());
        for (Unit unit : units) {
            if (unit.getJob() != realParam) {
                list.add(unit);
            }
        }
        return list;
    }
}
