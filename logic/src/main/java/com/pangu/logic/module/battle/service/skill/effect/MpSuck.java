package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Mp;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import org.springframework.stereotype.Component;

@Component
public class MpSuck implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.MP_SUCK;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final Integer mp = state.getParam(Integer.class);
        //计算mp扣减
        long targetMpChange = Math.min(target.getValue(UnitValue.MP), mp);
        if (targetMpChange == 0) return;
        context.addValue(target, AlterType.MP_SUCK, -targetMpChange);
        skillReport.add(time, target.getId(), new Mp(-targetMpChange));
        //扣多少吸多少。不受MP_ADD_RATE影响
        context.addValue(owner, AlterType.MP_SUCK, targetMpChange);
        skillReport.add(time, owner.getId(), new Mp(targetMpChange));
    }
}
