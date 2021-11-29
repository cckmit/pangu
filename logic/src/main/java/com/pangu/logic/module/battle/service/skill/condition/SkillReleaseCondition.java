package com.pangu.logic.module.battle.service.skill.condition;

import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;

/**
 * 在进行技能过滤的时候，判断技能能否进入选择列表
 */
public interface SkillReleaseCondition<T> {

    /**
     * 技能效果是否能够满足条件释放
     *
     * @param skillState
     * @param unit
     * @param time
     * @return
     */
    boolean valid(SkillState skillState, Unit unit, int time, T param);
}
