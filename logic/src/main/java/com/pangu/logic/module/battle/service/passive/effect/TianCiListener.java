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
 * 友军被暴击时为其添加BUFF
 */
@Component
public class TianCiListener implements UnitHpChangePassive {
    @Override
    public PassiveType getType() {
        return PassiveType.TIAN_CI_LISTENER;
    }

    @Override
    public void hpChange(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> changeUnit) {
        final BuffCastWhenBeCritParam param = passiveState.getParam(BuffCastWhenBeCritParam.class);

        for (Unit unit : changeUnit) {
            if (!owner.isFriend(unit)) {
                continue;
            }
            if (!context.isCrit(unit)) {
                continue;
            }

            final String buffId = param.getBuffId();
            final List<BuffState> buffStates = unit.getBuffBySettingId(buffId);
            if (buffStates.size()>=param.getMaxCount()) {
                continue;
            }

            BuffFactory.addBuff(buffId, owner, unit, time, damageReport, null);
        }
    }
}
