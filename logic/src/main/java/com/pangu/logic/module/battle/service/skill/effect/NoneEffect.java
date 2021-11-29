package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import org.springframework.stereotype.Component;

/**
 * 无效果，仅用于供前端表现
 */
@Component
public class NoneEffect implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.NONE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {

    }
}
