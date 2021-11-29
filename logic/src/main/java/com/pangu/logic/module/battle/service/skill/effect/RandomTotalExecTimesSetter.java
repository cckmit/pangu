package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.stereotype.Component;

/**
 * SkillEffectAction 总执行次数设置器：随机策略，仅伴随主效果首次执行时，执行一次
 */
@Component
public class RandomTotalExecTimesSetter implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.RANDOM_TOTAL_EXEC_TIMES_SETTER;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        if (context.getLoopTimes() > 1) {
            return;
        }

        final Integer[] range = state.getParam(Integer[].class);
        if (range == null || range.length != 2) {
            return;
        }

        context.getRootSkillEffectAction().setTotalExecTimes(RandomUtils.betweenInt(range[0], range[1], true));
    }
}
