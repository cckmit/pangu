package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 打印bestCircle区域战报<br>
 * 每个技能无论循环执行几次，只打印一次区域战报
 */
@Component
public class BestCircleAreaReportOnceGenerator implements SkillEffect {
    @Autowired
    private BestCircleAreaReportGenerator generator;

    @Override
    public EffectType getType() {
        return EffectType.BEST_CIRCLE_AREA_REPORT_ONCE_GENERATOR;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        if (context.getLoopTimes() != 1) {
            return;
        }

        generator.execute(state, owner, target, skillReport, time, skillState, context);
    }
}
