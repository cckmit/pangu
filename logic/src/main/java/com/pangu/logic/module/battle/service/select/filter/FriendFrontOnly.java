package com.pangu.logic.module.battle.service.select.filter;

import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.service.core.Fighter;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FriendFrontOnly implements Filter {
    @Override
    public List<Unit> filter(Unit unit, int time) {
        Fighter friend = unit.getFriend();
        List<Unit> current = friend.getCurrent();
        List<Unit> front = null;
        for (Unit cur : current) {
            if (cur.isDead() || cur.hasState(UnitState.UNVISUAL, time)) {
                continue;
            }
            if (cur.getSequence() == 0 || cur.getSequence() == 1) {
                if (front == null) {
                    front = new ArrayList<>(2);
                }
                front.add(cur);
            }
        }
        if (front == null) {
            return Collections.emptyList();
        }
        return front;
    }
}
