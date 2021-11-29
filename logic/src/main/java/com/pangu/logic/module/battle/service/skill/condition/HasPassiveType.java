package com.pangu.logic.module.battle.service.skill.condition;

import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveType;

public class HasPassiveType implements SkillReleaseCondition<PassiveType> {

    @Override
    public boolean valid(SkillState skillState, Unit unit, int time, PassiveType param) {
        return !unit.getPassiveStateByType(param).isEmpty();
    }
}
