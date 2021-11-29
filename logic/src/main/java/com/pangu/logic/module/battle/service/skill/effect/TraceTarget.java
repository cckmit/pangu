package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import org.springframework.stereotype.Component;

/**
 * 追踪目标一定时间
 */
@Component
public class TraceTarget implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.TRACE_TARGET;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        owner.addState(UnitState.ZHUI_JI, time + state.getParam(int.class));
        owner.setTarget(target);
        owner.setTraceUnit(target);
    }
}
