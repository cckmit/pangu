package com.pangu.logic.module.battle.service.action.custom;

import com.pangu.logic.module.battle.service.action.Action;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import lombok.AllArgsConstructor;

/**
 * 该行为主要用于在指定时刻执行特定技能
 */
@AllArgsConstructor
public class ScheduledSkillUpdateAction implements Action {
    private int time;
    private SkillState skillState;
    private Unit owner;

    @Override
    public int getTime() {
        return time;
    }

    @Override
    public void execute() {
        SkillFactory.updateNextExecuteSkill(time, owner, skillState);
    }
}
