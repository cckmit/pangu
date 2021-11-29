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
import org.springframework.stereotype.Component;

/**
 * 嘲讽技能效果
 */
@Component
public class Sneer implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.SNEER;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {

        // 设置讽刺对象
        target.setTraceUnit(owner);
        StateAddParam param = state.getParam(StateAddParam.class);
        UnitState type = param.getState();
        int validTime = param.getTime();

        SkillUtils.addState(owner, target, type, time, validTime + time, skillReport, context);
    }
}
