package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.AddPassive;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveFactory;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import org.springframework.stereotype.Component;

/**
 * 添加共享性被动
 */
@Component
public class AddCommonPassive implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.COMMON_PASSIVE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        PassiveState addition = state.getAddition(PassiveState.class);
        if (addition == null) {
            final String param = state.getParam(String.class);
            final PassiveState passiveState = PassiveFactory.initState(param, time);
            addition = passiveState;
            state.setAddition(passiveState);
        }
        target.addPassive(addition, owner);
        skillReport.add(time, target.getId(), new AddPassive(addition.getId()));
    }
}
