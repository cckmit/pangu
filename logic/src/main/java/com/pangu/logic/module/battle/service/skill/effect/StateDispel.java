package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.StateRemove;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StateDispel implements SkillEffect {

    @Override
    public EffectType getType() {
        return EffectType.STATE_DISPEL;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time,
                        SkillState skillState, Context context) {
        UnitState[] states = state.getParam(UnitState[].class);
        List<UnitState> removeStates = new ArrayList<>(states.length);
        for (UnitState us : states) {
            if (target.hasState(us, time)) {
                target.removeState(us);
                removeStates.add(us);
            }
        }
        if (removeStates.size() != 0) {
            skillReport.add(time, target.getId(), new StateRemove(removeStates));
        }
    }

}
