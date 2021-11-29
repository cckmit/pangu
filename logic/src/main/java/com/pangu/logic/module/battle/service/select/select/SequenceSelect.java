package com.pangu.logic.module.battle.service.select.select;

import com.fasterxml.jackson.core.type.TypeReference;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.core.Unit;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SequenceSelect implements Selector {
    private final static TypeReference<ArrayList<Integer>> paramType = new TypeReference<ArrayList<Integer>>() {
    };

    @Override
    public List<Unit> select(Unit owner, List<Unit> units, SelectSetting selectSetting, int time) {
        final List<Integer> realParam = selectSetting.getRealParam(paramType);
        if (CollectionUtils.isEmpty(realParam)) {
            return Collections.emptyList();
        }
        List<Unit> result = null;
        for (Unit unit : units) {
            if (realParam.contains(unit.getSequence())) {
                if (result == null) {
                    result = new ArrayList<>(realParam.size());
                }
                result.add(unit);
            }
        }
        return result;
    }
}
