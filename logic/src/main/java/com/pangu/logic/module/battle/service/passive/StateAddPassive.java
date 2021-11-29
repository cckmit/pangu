package com.pangu.logic.module.battle.service.passive;

import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;

/**
 * 主动添加异常状态时触发的被动
 */
public interface StateAddPassive extends Passive {
    /**
     * 添加异常状态成功的后置处理器
     * 此方法内部禁止调用context.addState(),会造成无限递归
     *
     * @param passiveState
     * @param owner        异常状态释放者
     * @param state
     * @param time         当前时间
     * @param validTime    状态过期时间
     * @param context
     * @param damageReport
     * @return 调整后的状态过期时间
     */
    void stateAddAfter(PassiveState passiveState, Unit owner, Unit target, UnitState state, int time, int validTime, Context context, ITimedDamageReport damageReport);
}
