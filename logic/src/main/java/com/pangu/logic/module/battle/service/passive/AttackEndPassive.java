package com.pangu.logic.module.battle.service.passive;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;

/**
 * 攻击结束后阶段（触发完防守方被动后）
 */
public interface AttackEndPassive extends Passive{
    /**
     * 执行攻击他人并造成伤害
     *  @param passiveState
     * @param owner
     * @param target 攻击目标
     * @param damage
     * @param time
     * @param context
     * @param skillState
     */
    void attackEnd(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport);
}
