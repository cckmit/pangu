package com.pangu.logic.module.battle.service.skill.condition;

import com.pangu.logic.module.battle.resource.BuffSetting;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;

/**
 * 当身上不存在某个buff的时候触发
 */
public class NoBuff implements SkillReleaseCondition<String> {
    @Override
    public boolean valid(SkillState skillState, Unit unit, int time, String param) {
        BuffSetting setting = BuffFactory.getSetting(param);
        BuffState buffStateByTag = unit.getBuffStateByTag(setting.getTag());
        return buffStateByTag == null;
    }
}
