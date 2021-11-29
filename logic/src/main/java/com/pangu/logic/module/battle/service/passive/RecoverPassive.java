package com.pangu.logic.module.battle.service.passive;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;

/**
 * 被治疗触发被动
 */
public interface RecoverPassive extends Passive {

    /**
     * 被治疗
     * @param passiveState
     * @param owner     被治疗者（被动持有者）
     * @param from      触发该被动的施法者
     * @param recover
     * @param time
     * @param context
     * @param skillState
     * @param skillReport
     */
    void recover(PassiveState passiveState, Unit owner, Unit from, long recover, int time, Context context, SkillState skillState, SkillReport skillReport);
}
