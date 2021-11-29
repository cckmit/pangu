package com.pangu.logic.module.battle.service.select.filter;

import com.pangu.logic.module.battle.service.core.Unit;

import java.util.List;

/**
 * 筛选出除自身外的所有目标
 */
public class AllButSelf implements Filter{
    @Override
    public List<Unit> filter(Unit unit, int time) {
        final AllFilter allFilter = new AllFilter();
        final List<Unit> all = allFilter.filter(unit, time);
        all.remove(unit);
        return all;
    }
}
