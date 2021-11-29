package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillSelectPassive;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import org.springframework.stereotype.Component;

/**
 * 普攻替换
 */
@Component
public class ReplaceNormalSkill implements SkillSelectPassive {
    @Override
    public SkillState skillSelect(PassiveState passiveState, SkillState skillState, Unit owner, int time) {
        if (skillState.getType() != SkillType.NORMAL) {
            return null;
        }
        passiveState.addCD(time);

        String skillId = passiveState.getParam(String.class);
        return SkillFactory.initState(skillId);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.REPLACE_NORMAL_SKILL;
    }
}
