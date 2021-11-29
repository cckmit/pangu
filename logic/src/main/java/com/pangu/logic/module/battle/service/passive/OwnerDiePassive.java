package com.pangu.logic.module.battle.service.passive;

import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;

/**
 * 自身死亡后触发
 */
public interface OwnerDiePassive extends Passive {
    /**
     * @param passiveState
     * @param owner        自身（已死亡单元）
     * @param attack       攻击方（造成自己死亡的单元）
     * @param time         当前世界时间
     * @param context      当前上下文
     */
    void die(PassiveState passiveState, Unit owner, Unit attack, ITimedDamageReport timedDamageReport, int time, Context context);
}
