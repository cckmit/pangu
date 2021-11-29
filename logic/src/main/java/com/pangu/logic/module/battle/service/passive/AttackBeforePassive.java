package com.pangu.logic.module.battle.service.passive;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.Unit;

/**
 * 攻击目标之前释放
 */
public interface AttackBeforePassive extends Passive {

    /**
     * 攻击之前
     *
     * @param passiveState
     * @param owner
     * @param target
     * @param time
     * @param context
     * @param skillReport
     */
    void attackBefore(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time, Context context, SkillReport skillReport);

    /**
     * 攻击之前
     *
     * @param passiveState
     * @param owner
     * @param target
     * @param time
     * @param context
     * @param skillReport
     */
    void attackEnd(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time, Context context, SkillReport skillReport);

}
