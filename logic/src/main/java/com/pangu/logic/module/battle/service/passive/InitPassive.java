package com.pangu.logic.module.battle.service.passive;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;

/**
 * 初始化被动，常常用与添加属性
 */
public interface InitPassive extends Passive {

    /**
     * 初始化
     *
     * @param passiveState
     * @param owner
     * @param context
     * @param skillReport
     */
    void init(int time, PassiveState passiveState, Unit owner, Context context, SkillReport skillReport);
}
