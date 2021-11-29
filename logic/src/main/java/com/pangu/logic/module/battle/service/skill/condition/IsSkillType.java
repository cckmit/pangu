package com.pangu.logic.module.battle.service.skill.condition;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;

public class IsSkillType implements SkillReleaseCondition<SkillType> {

    @Override
    public boolean valid(SkillState skillState, Unit unit, int time, SkillType param) {
        return skillState.getType() == param;
    }
}
