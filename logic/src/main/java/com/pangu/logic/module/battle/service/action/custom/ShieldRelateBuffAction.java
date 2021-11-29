package com.pangu.logic.module.battle.service.action.custom;

import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.service.action.CloseableAction;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.List;

/**
 * 护盾与BUFF关联，如果护盾没了，相关BUFF会被移除
 * @author Kubby
 */
public class ShieldRelateBuffAction extends CloseableAction {

    private int time;

    private Unit owner;

    private List<BuffState> buffStates;

    @Override
    public int getTime() {
        return time;
    }

    @Override
    public void execute() {
        if (isDone()) {
            return;
        }
        if (owner.getValue(UnitValue.SHIELD) <= 0) {
            cancel(time);
        } else {
            time += 1000;
            owner.addTimedAction(this);
        }
    }

    public void cancel(int time) {
        if (isDone()) {
            return;
        }
        done();
        for (BuffState buffState : buffStates) {
            BuffFactory.removeBuffState(buffState.getId(), owner, time);
        }
    }

    public static ShieldRelateBuffAction of(int time, Unit owner, List<BuffState> buffStates) {
        ShieldRelateBuffAction action = new ShieldRelateBuffAction();
        action.time = time;
        action.owner = owner;
        action.buffStates = buffStates;
        return action;
    }
}
