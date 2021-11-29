package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Death;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import org.springframework.stereotype.Component;

/**
 * 无视一切效果强行杀死，不触发任何被动<br/>
 * 该效果易引起死循环，废弃
 */
@Component
@Deprecated
public class Suicide implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.SUICIDE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        owner.setDead(true);
        owner.setValue(UnitValue.HP, 0L);
        skillReport.add(time, target.getId(), new Death());
    }
}
