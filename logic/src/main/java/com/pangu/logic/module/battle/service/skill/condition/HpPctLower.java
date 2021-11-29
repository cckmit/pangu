package com.pangu.logic.module.battle.service.skill.condition;

import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;

public class HpPctLower implements SkillReleaseCondition<Double> {

    @Override
    public boolean valid(SkillState skillState, Unit unit, int time, Double param) {
        return unit.getHpPct() < param;
    }
}
