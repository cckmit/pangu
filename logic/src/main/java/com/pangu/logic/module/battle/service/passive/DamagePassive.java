package com.pangu.logic.module.battle.service.passive;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;

/**
 * 受伤单元执行被动
 */
public interface DamagePassive extends Passive {

    /**
     * 受伤
     * @param passiveState
     * @param owner     受击目标（被动持有者）
     * @param damage
     * @param attacker  攻击方
     * @param time
     * @param context
     * @param skillState
     * @param skillReport
     */
    void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport);
}
