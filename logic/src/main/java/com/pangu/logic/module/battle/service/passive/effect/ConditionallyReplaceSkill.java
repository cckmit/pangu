package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillSelectPassive;
import com.pangu.logic.module.battle.service.passive.param.ConditionallyReplaceSkillParam;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import com.pangu.logic.module.battle.service.skill.condition.ConditionType;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 附带通用条件约束逻辑的替换普攻被动
 */

@Component
public class ConditionallyReplaceSkill implements SkillSelectPassive {
    @Override
    public PassiveType getType() {
        return PassiveType.CONDITIONALLY_REPLACE_SKILL;
    }

    @Override
    public SkillState skillSelect(PassiveState passiveState, SkillState skillState, Unit owner, int time) {
        //新增条件判断
        final ConditionallyReplaceSkillParam param = passiveState.getParam(ConditionallyReplaceSkillParam.class);
        for (Map.Entry<ConditionType, Object> entry : param.getRealConditions().entrySet()) {
            final ConditionType key = entry.getKey();
            final Object value = entry.getValue();
            final boolean pass = key.valid(skillState, owner, time, value);
            if (!pass) {
                return null;
            }
        }

        passiveState.addCD(time);

        String skillId = param.getSkillId();
        return SkillFactory.initState(skillId);
    }
}
