package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillSelectPassive;
import com.pangu.logic.module.battle.service.passive.param.ReplaceSkillByTransformStateParam;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import org.springframework.stereotype.Component;

/**
 * 根据变身状态替换技能
 */
@Component
public class ReplaceSkillByTransformState implements SkillSelectPassive {
    @Override
    public PassiveType getType() {
        return PassiveType.REPLACE_SKILL_BY_TRANSFORM_STATE;
    }

    @Override
    public SkillState skillSelect(PassiveState passiveState, SkillState skillState, Unit owner, int time) {
        final ReplaceSkillByTransformStateParam param = passiveState.getParam(ReplaceSkillByTransformStateParam.class);
        if (owner.getTransformState() != param.getTriggerState()) {
            return null;
        }
        if (!skillState.getId().equals(param.getTriggerId())) {
            return null;
        }
        return SkillFactory.initState(param.getReplacingId());
    }
}
