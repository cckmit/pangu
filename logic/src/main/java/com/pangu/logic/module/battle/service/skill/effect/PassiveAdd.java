package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveFactory;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 用于增加被动
 */
@Component
public class PassiveAdd implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.PASSIVE_ADD;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        String[] passiveIds = state.getParam(String[].class);
        Map<String, PassiveState> passiveStatesById = target.getPassiveStatesById();
        for (String passiveId : passiveIds) {
            if (passiveStatesById.containsKey(passiveId)) {
                continue;
            }
            PassiveState passiveState = PassiveFactory.initState(passiveId, time);
            target.addPassive(passiveState, owner);
//            skillReport.add(time, target.getId(), new AddPassive(passiveId));
        }
    }
}
