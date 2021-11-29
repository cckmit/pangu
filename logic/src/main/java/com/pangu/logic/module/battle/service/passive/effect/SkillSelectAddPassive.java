package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveFactory;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillSelectPassive;
import com.pangu.logic.module.battle.service.passive.param.SkillSelectAddPassiveParam;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.stereotype.Component;

@Component
public class SkillSelectAddPassive implements SkillSelectPassive {
    @Override
    public PassiveType getType() {
        return PassiveType.SKILL_SELECT_ADD_PASSIVE;
    }

    @Override
    public SkillState skillSelect(PassiveState passiveState, SkillState skillState, Unit owner, int time) {
        final SkillSelectAddPassiveParam param = passiveState.getParam(SkillSelectAddPassiveParam.class);
        final SkillType type = param.getType();
        if (type != null && type != skillState.getType()) {
            return skillState;
        }
        double rate = param.getRate();
        if (rate > 0 && rate < 1 && !RandomUtils.isHit(rate)) {
            return skillState;
        }
        for (String passiveId : param.getPassives()) {
            final PassiveState addPassiveState = PassiveFactory.initState(passiveId, time);
            owner.addPassive(addPassiveState, owner);
        }

        return null;
    }
}
