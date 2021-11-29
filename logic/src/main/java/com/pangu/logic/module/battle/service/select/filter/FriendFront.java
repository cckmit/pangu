package com.pangu.logic.module.battle.service.select.filter;

import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.service.core.Fighter;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.ArrayList;
import java.util.List;

/**
 * 友军前排
 */
public class FriendFront implements Filter {
    @Override
    public List<Unit> filter(Unit unit, int time) {
        Fighter friend = unit.getFriend();
        List<Unit> current = friend.getCurrent();
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
