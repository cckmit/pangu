package com.pangu.logic.module.battle.service.select.filter;

import com.pangu.logic.module.battle.service.core.Unit;

import java.util.ArrayList;
import java.util.List;

/**
 * 目标过滤
 * author weihongwei
 * date 2018/3/27
 */
public interface Filter {

    /**
     * @param unit
     * @return
     */
    List<Unit> filter(Unit unit, int time);


    default List<Unit> list(Unit unit, int time) {
        List<Unit> all = filter(unit, time);
        List<Unit> select = new ArrayList<>(all.size());
        for (Unit item : all) {
            if (item == null || !item.canSelect(time)) {
                continue;
            }
            select.add(item);
        }
        return select;
    }

}
