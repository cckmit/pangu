package com.pangu.logic.module.battle.service.action;

import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.core.Unit;
import lombok.Getter;
import lombok.Setter;

/**
 * Buff行动，用于执行buff的添加、生效、移除
 */
@Getter
@Setter
public class BuffAction extends CloseableAction {

    // 时间
    private int time;

    // buff状态
    private final BuffState buffState;

    // buff在谁身上
    private final Unit owner;

    // 移除时间
    private int removeTime;

    public BuffAction(int time, BuffState buffState, Unit owner, int removeTime) {
        this.time = time;
        this.buffState = buffState;
        this.owner = owner;
        this.removeTime = removeTime;
    }

    @Override
    public void execute() {
        BuffType type = buffState.getType();
        Buff buff = BuffFactory.getBuff(type);
        if (time >= removeTime) {
            if (time == removeTime && buffState.getInterval() > 0) {
                buff.update(buffState, owner, time, null);
            }
            BuffFactory.doRemoveBuffState(buffState, owner, time);
            return;
        }
        buff.update(buffState, owner, time, null);

        int interval = buffState.getInterval();
        if (interval <= 0) {
            time = removeTime;
        } else {
            time += interval;
        }
        owner.addTimedAction(this);
    }
}
