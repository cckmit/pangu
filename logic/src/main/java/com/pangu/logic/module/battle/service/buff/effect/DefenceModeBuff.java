package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.DynamicRemoveOnceParam;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import org.springframework.stereotype.Component;

/**
 * 当生命值低于40%时进入防御状态，站在原地无法移动，防御力提升50%，普通攻击范围增加，每3秒恢复1%最大生命值，当前生命值超过75%时，解除防御状态
 */
@Component
public class DefenceModeBuff extends Once {
    @Override
    public BuffType getType() {
        return BuffType.DYNAMIC_REMOVE_ONCE;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object addition) {
        final DynamicRemoveOnceParam param = state.getParam(DynamicRemoveOnceParam.class);
        if (!condVerify(state, unit, param.getUpdateCond(), time)) {
            return;
        }
        final Unit caster = state.getCaster();
        final BuffReport buffReport = state.getBuffReport();
        final Context context = new Context(caster);
        context.modVal(caster, unit, time, buffReport, param.getUpdateValModParam(), null, null);
        context.execute(time, buffReport);

        if (!condVerify(state, unit, param.getRemoveCond(), time)) {
            return;
        }
        BuffFactory.removeBuffState(state, unit, time);
    }
}
