package com.pangu.logic.module.battle.service.select.filter;

import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.service.core.Fighter;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EnemyBackOnly implements Filter {

    @Override
    public List<Unit> filter(Unit unit, int time) {
        Fighter enemy = unit.getEnemy();
        List<Unit> current = enemy.getCurrent();
        List<Unit> backs = null;
        for (Unit cur : current) {
            if (cur.isDead() || cur.hasState(UnitState.UNVISUAL, time)) {
                continue;
            }
            if (cur.getSequence() > 1) {
                if (backs == null) {
                    backs = new ArrayList<>(4);
                }
                backs.add(cur);
            }
        }
        if (backs == null) {
            return Collections.emptyList();
        }
        return backs;
    }
}
