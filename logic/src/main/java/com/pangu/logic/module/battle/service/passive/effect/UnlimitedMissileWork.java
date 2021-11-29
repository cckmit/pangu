package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveFactory;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.UnlimitedMissileWorkParam;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import org.springframework.stereotype.Component;

/**
 * 当累积的层数达到40层时，后续的所有普通攻击全是导弹攻击
 */
@Component
public class UnlimitedMissileWork extends TimesReplaceNormalSkill {
    @Override
    public PassiveType getType() {
        return PassiveType.UNLIMITED_MISSILE_WORK;
    }

    @Override
    public SkillState skillSelect(PassiveState passiveState, SkillState skillState, Unit owner, int time) {
        final SkillState replacingSkill = super.skillSelect(passiveState, skillState, owner, time);
        if (replacingSkill != null) {
            return replacingSkill;
        }
        final UnlimitedMissileWorkParam param = passiveState.getParam(UnlimitedMissileWorkParam.class);
        final PassiveState state = owner.getPassiveStates(param.getTriggerPassive());
        if (state.getType() != PassiveType.KUANG_BAO) {
            return null;
        }
        final KuangBao passive = PassiveFactory.getPassive(state.getType());
        if (passive.getBonusCount(state) < param.getUnlimitedTriggerTimes()) {
            return null;
        }
        return SkillFactory.initState(param.getSkillId());
    }
}
