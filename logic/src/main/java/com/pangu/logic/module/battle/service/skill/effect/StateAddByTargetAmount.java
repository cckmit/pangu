package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.StateAddByTargetAmountParam;
import com.pangu.logic.module.battle.service.skill.utils.SkillUtils;
import org.springframework.stereotype.Component;

/**
 * 若范围内只有1个敌军时，则将该敌军冰冻2秒
 */
@Component
public class StateAddByTargetAmount implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.STATE_ADD_BY_TARGET_AMOUNT;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        StateAddByTargetAmountParam param = state.getParam(StateAddByTargetAmountParam.class);
        if (context.getTargetAmount() > param.getTargetAmount()) {
            return;
        }
        UnitState type = param.getState();
        if (target.hasStateImmune(type, time)) {
            return;
        }
        int validTime = param.getTime();

        SkillUtils.addState(owner, target, type, time, validTime + time, skillReport, context);
    }
}
