package com.pangu.logic.module.battle.service.skill.condition;

import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;

public class TimeLapse implements SkillReleaseCondition<Integer> {
    @Override
    public boolean valid(SkillState skillState, Unit unit, int time, Integer param) {
        return time >= param;
    }
}
