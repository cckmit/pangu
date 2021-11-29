package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import com.pangu.logic.module.battle.service.skill.utils.SkillUtils;

/**
 * 定时为BUFF持有者添加异常状态
 */
public class IntervalState implements Buff {
    @Override
    public BuffType getType() {
        return BuffType.INTERVAL_STATE;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object addition) {
        final StateAddParam param = state.getParam(StateAddParam.class);

        final BuffReport buffReport = state.getBuffReport();
        final Unit caster = state.getCaster();
        final Context context = new Context(caster);
        SkillUtils.addState(caster, unit, param.getState(), time, param.getTime() + time, buffReport, context);
        context.execute(time, buffReport);
    }
}
