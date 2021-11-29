package com.pangu.logic.module.battle.service.passive;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;

public interface RecoverTargetPassive extends Passive{
    /**
     * 治疗别人
     *
     * @param passiveState
     * @param owner        释放技能者
     * @param target       被治疗者
     * @param recover      回复量
     * @param time
     * @param context
     * @param skillState
     * @param skillReport
     */
    void recoverTarget(PassiveState passiveState, Unit owner, Unit target, long recover, int time, Context context, SkillState skillState, SkillReport skillReport);
}
