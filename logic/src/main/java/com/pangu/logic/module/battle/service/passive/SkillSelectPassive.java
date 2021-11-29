package com.pangu.logic.module.battle.service.passive;

import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;

/**
 * 技能行动选择后触发
 */
public interface SkillSelectPassive extends Passive {

    /**
     * @param passiveState
     * @param skillState
     * @param owner
     * @param time
     * @return
     */
    SkillState skillSelect(PassiveState passiveState, SkillState skillState, Unit owner, int time);
}
