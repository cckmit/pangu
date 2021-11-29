package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.action.custom.DamageOverTimeAction;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.DamageOverTimeParam;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * 持续性伤害效果
 */
@Component
public class DamageOverTime implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.DAMAGE_OVER_TIME;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final DamageOverTimeParam param = state.getParam(DamageOverTimeParam.class);
        final DamageOverTimeAction dotAction = new DamageOverTimeAction(time+param.getInterval(), owner, skillState, skillReport, state,  new ArrayList<Unit>(){{add(target);}});
        if (skillState.getSetting().isIgnoreDie()) {
            owner.getBattle().addWorldAction(dotAction);
        } else {
            owner.addTimedAction(dotAction);
        }
    }
}
