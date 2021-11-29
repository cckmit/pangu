package com.pangu.logic.module.battle.service.passive;

import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.Set;

/**
 * 当战场上其他战斗单元被攻击时触发此被动
 */
public interface UnitHpChangePassive extends Passive {

    /**
     * 该方法禁止扣减HP
     * @param passiveState 被动技能
     * @param owner        被动拥有者
     * @param attacker     攻击方
     * @param time         当前时间
     * @param context      执行上下文
     * @param damageReport  技能战报
     * @param changeUnit        血量变更的单元
     */
    void hpChange(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> changeUnit);
}
