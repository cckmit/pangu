package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.StateRemove;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.DispelType;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 移除负面状态以及buff
 */
@Component
public class BuffDispel implements SkillEffect {

    @Override
    public EffectType getType() {
        return EffectType.BUFF_DISPEL;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        DispelType dispelType = state.getParam(DispelType.class);
        List<UnitState> removeStates = BuffFactory.dispelState(target, dispelType, time);
        if (removeStates.size() != 0) {
            skillReport.add(time, target.getId(), new StateRemove(removeStates));
        }
    }

}
