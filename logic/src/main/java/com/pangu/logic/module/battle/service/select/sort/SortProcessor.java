package com.pangu.logic.module.battle.service.select.sort;

import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.List;

/**
 * author weihongwei
 * date 2018/3/27
 */
public interface SortProcessor {

    /**
     * 排序
     *
     * @param position
     * @param units
     * @return
     */
    List<Unit> sort(Point position, List<Unit> units);
}
