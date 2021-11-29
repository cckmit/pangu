package com.pangu.logic.module.battle.service.select.select;

import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.core.Fighter;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.Collections;
import java.util.List;

/**
 * 选取最多敌军的英雄
 */
public class MostEnemySelect implements Selector {
    @Override
    public List<Unit> select(Unit owner, List<Unit> units, SelectSetting selectSetting, int time) {
        if (units.isEmpty()) {
            return Collections.emptyList();
        }
        int distance = selectSetting.getWidth();
        int amount = -1;
        Unit bestUnit = null;
        for (Unit unit : units) {
            Point from = unit.getPoint();
            Fighter enemy = unit.getEnemy();
            List<Unit> current = enemy.getCurrent();
            if (current.isEmpty()) {
                if (amount < 0) {
                    amount = 0;
                    bestUnit = unit;
                }
                continue;
            }
            int curAmount = 0;
            for (Unit item : current) {
                int curDis = from.distance(item.getPoint());
                if (curDis <= distance) {
                    ++curAmount;
                }
            }
            if (curAmount >= amount) {
                amount = curAmount;
                bestUnit = unit;
            }
        }
        return Collections.singletonList(bestUnit);
    }
}
