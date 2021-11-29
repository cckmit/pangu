package com.pangu.logic.module.battle.service.passive;

import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;

/**
 * 被添加异常状态时触发的被动
 */
public interface BeStateAddPassive extends Passive {
    /**
     * 被添加异常状态前置处理器
     * 此方法内部禁止调用context.addState(),会造成无限递归
     *
     * @param passiveState
     * @param owner     被添加异常状态的目标
     * @param state
     * @param time      当前时间
     * @param validTime 状态过期时间
     * @param context
     * @param damageReport
     * @return 调整后的状态过期时间
     */
    int beStateAddBefore(PassiveState passiveState, Unit owner, UnitState state, int time, int validTime, Context context, ITimedDamageReport damageReport);
}
