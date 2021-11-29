package com.pangu.logic.module.battle.service.select.select;

import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.core.Unit;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class HpPctAvoidBaTiSelect implements Selector {
    @Override
    public List<Unit> select(Unit owner, List<Unit> units, SelectSetting selectSetting, int time) {
        HpPct.HpPctParam param = selectSetting.getRealParam(HpPct.HpPctParam.class);
        List<Unit> result = new LinkedList<>();
        for (Unit unit : units) {
            double hpPct = unit.getHpPct();
            if (param.lower && hpPct <= param.pct) {
                result.add(unit);
            }
            if (!param.lower && hpPct >= param.pct) {
                result.add(unit);
            }
        }

        final List<Unit> controllableCandidates = result.stream().filter(unit -> !unit.immuneControl(time)).collect(Collectors.toList());
        if (!controllableCandidates.isEmpty()) {
            return controllableCandidates;
        }

        return result;
    }

    @Getter
    public static class HpPctParam {

        double pct;

        boolean lower;

    }
}
