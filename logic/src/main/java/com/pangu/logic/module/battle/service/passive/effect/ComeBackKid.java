package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.OwnerDiePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import com.pangu.logic.utils.ExpressionHelper;
import com.pangu.framework.utils.reflect.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * 死里逃生
 */
@Component
public class ComeBackKid implements OwnerDiePassive {
    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attack, ITimedDamageReport timedDamageReport, int time, Context context) {
        final String recoverExp = passiveState.getParam(String.class);
        if (StringUtils.isEmpty(recoverExp)) {
            return;
        }
        final boolean success = owner.revive(time);
        if (!success) {
            return;
        }

        //计算回血值
        final HashMap<String, Object> mapCtx = new HashMap<>(2);
        mapCtx.put("owner", owner);
        mapCtx.put("attacker", attack);
        final Long hpChange = ExpressionHelper.invoke(recoverExp, Long.class, mapCtx);

        //回复生命
        final long value = owner.getValue(UnitValue.HP) + context.getHpChange(owner);
        context.addPassiveValue(owner, AlterType.HP, -value + 1);
        PassiveUtils.hpUpdate(context, timedDamageReport, owner, owner, hpChange, time, passiveState);
        passiveState.addCD(time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.COME_BACK_KID;
    }
}
