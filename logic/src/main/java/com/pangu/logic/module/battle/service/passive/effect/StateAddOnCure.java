package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.RecoverTargetPassive;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import org.springframework.stereotype.Component;

/**
 * 治疗目标时为其添加状态
 */
@Component
public class StateAddOnCure implements RecoverTargetPassive {
    @Override
    public PassiveType getType() {
        return PassiveType.STATE_ADD_ON_CURE;
    }

    @Override
    public void recoverTarget(PassiveState passiveState, Unit owner, Unit target, long recover, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final StateAddParam param = passiveState.getParam(StateAddParam.class);
        PassiveUtils.addState(owner, target, param.getState(), param.getTime() + time, time, passiveState, context, skillReport);
        passiveState.addCD(time);
    }
}
