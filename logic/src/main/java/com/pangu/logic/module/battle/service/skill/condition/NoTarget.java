package com.pangu.logic.module.battle.service.skill.condition;

import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.RangeHelper;

/**
 * 没有目标时才释放
 */
public class NoTarget implements SkillReleaseCondition<Void> {
    @Override
    public boolean valid(SkillState skillState, Unit unit, int time, Void param) {
        return null == RangeHelper.getTarget(unit, true, time);
    }
}
