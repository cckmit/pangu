package com.pangu.logic.module.battle.service.select.filter;

import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.service.core.Fighter;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.ArrayList;
import java.util.List;

/**
 * 敌军前排
 */
public class EnemyFront implements Filter {
    @Override
    public List<Unit> filter(Unit unit, int time) {
        Fighter enemy = unit.getEnemy();
        List<Unit> current = enemy.getCurrent();
        List<Unit> front = null;
        List<Unit> valid = new ArrayList<>(current.size());
        for (Unit cur : current) {
            if (cur.isDead() || cur.hasState(UnitState.UNVISUAL, time)) {
                continue;
            }
            valid.add(cur);
            if (cur.getSequence() == 0 || cur.getSequence() == 1) {
                if (front == null) {
                    front = new ArrayList<>(2);
                }
                front.add(cur);
            }
        }
        if (front == null) {
            front = valid;
        }
        return front;
    }
}
