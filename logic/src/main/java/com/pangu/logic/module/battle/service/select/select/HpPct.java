package com.pangu.logic.module.battle.service.select.select;

import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.core.Unit;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

/**
 * 根据血量百分比选择
 * @author Kubby
 */
public class HpPct implements Selector {

    @Override
    public List<Unit> select(Unit owner, List<Unit> units, SelectSetting selectSetting, int time) {
        HpPctParam param = selectSetting.getRealParam(HpPctParam.class);
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
        return result;
    }

    @Getter
    public static class HpPctParam {

        double pct;

        boolean lower;

    }
}
