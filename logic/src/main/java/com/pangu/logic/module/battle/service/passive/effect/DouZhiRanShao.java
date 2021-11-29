package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillSelectPassive;
import com.pangu.logic.module.battle.service.passive.param.DouZhiRanShaoParam;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import org.springframework.stereotype.Component;

/**
 * 斗志燃烧
 * @author Kubby
 */
@Component
public class DouZhiRanShao implements SkillSelectPassive {

    @Override
    public SkillState skillSelect(PassiveState passiveState, SkillState skillState, Unit owner, int time) {
        if (skillState.getType() != SkillType.NORMAL) {
            return null;
        }
        DouZhiRanShaoParam param = passiveState.getParam(DouZhiRanShaoParam.class);
        if (owner.getBuffBySettingId(param.getBuffTag()).isEmpty()) {
            return null;
        }

        return SkillFactory.initState(param.getSkillId());
    }

    @Override
    public PassiveType getType() {
        return PassiveType.DOU_ZHI_RAN_SHAO;
    }
}
