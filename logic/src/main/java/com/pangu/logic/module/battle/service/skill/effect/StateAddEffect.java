package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import com.pangu.logic.module.battle.service.skill.utils.SkillUtils;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.stereotype.Component;

/**
 * 给战斗单元添加状态
 */
@Component
public class StateAddEffect implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.STATE_ADD;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        StateAddParam param = state.getParam(StateAddParam.class);
        double rate = param.getRate();
        if (rate > 0 && rate < 1) {
            if (!RandomUtils.isHit(rate)) {
                return;
            }
        }

        UnitState type = param.getState();
        if (target.hasStateImmune(type, time)) {
            return;
        }
        int validTime = param.getTime();

        SkillUtils.addState(owner, target, type, time, validTime + time, skillReport, context);
    }
}
