package com.pangu.logic.module.battle.service.select.sort;

import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.List;

/**
 * 攻击力从高到底排序
 */
public class ATK_High implements SortProcessor{
    @Override
    public List<Unit> sort(Point position, List<Unit> units) {
        units.sort((a, b) -> Long.compare(b.getHighestATK(), a.getHighestATK()));
        return units;
    }
}
