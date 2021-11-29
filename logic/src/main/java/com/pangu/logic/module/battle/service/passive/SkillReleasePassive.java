package com.pangu.logic.module.battle.service.passive;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;

/**
 * 其他人释放大招时触发
 */
public interface SkillReleasePassive extends Passive {

    /**
     * 其他人释放大招
     *
     * @param passiveState
     * @param owner
     * @param attacker
     * @param skillState
     * @param time
     * @param context
     * @param skillReport
     */
    void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time, Context context, SkillReport skillReport);
}
