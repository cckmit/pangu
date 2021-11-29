package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitHpChangePassive;
import com.pangu.logic.module.battle.service.passive.param.BuffCastWhenBeCritParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 受到暴击时，为自己添加BUFF
 * 在生命变更阶段做处理，是因为buff产生的暴击效果也需要被考量在内
 */
@Component
public class BuffCastWhenBeCrit implements UnitHpChangePassive {
    @Override
    public PassiveType getType() {
        return PassiveType.BUFF_CAST_WHEN_BE_CRIT;
    }

    @Override
    public void hpChange(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> changeUnit) {
        if (!context.isCrit(owner)) {
            return;
        }

        final BuffCastWhenBeCritParam param = passiveState.getParam(BuffCastWhenBeCritParam.class);
        final String buffId = param.getBuffId();
        final List<BuffState> buffStates = owner.getBuffBySettingId(buffId);
        if (buffStates.size() >= param.getMaxCount()) {
            return;
        }

        BuffFactory.addBuff(buffId, owner, owner, time, damageReport, null);
        passiveState.addCD(time);
    }
}
