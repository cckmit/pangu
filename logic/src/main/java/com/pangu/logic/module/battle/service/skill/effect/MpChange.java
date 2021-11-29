package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Mp;
import com.pangu.logic.module.battle.service.alter.MpAlter;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import org.springframework.stereotype.Component;

/**
 * 能量变更通用类
 */
@Component
public class MpChange implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.MP_CHANGE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final Integer mp = state.getParam(Integer.class);
        final long trueMpChange = MpAlter.calMpChange(target, mp);
        if (trueMpChange == 0) return;
        context.addValue(target, AlterType.MP, mp);
        skillReport.add(time, target.getId(), new Mp(trueMpChange));
    }
}
