package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.DispelType;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.OwnerDiePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.ReviveParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 复活并给自己添加BUFF
 */
@Component
public class Revive implements OwnerDiePassive {
    @Override
    public PassiveType getType() {
        return PassiveType.REVIVE;
    }

    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attack, ITimedDamageReport timedDamageReport, int time, Context context) {
        // 清理掉所有buff
        List<BuffState> harmBuff = owner.getBuffByDispel(DispelType.HARMFUL);
        if (harmBuff != null && harmBuff.size() != 0) {
            for (BuffState buffState : harmBuff.toArray(new BuffState[0])) {
                BuffFactory.removeBuffState(buffState, owner, time);
            }
        }

        final boolean success = owner.revive(time);
        if (!success) {
            return;
        }

        final long value = owner.getValue(UnitValue.HP) + context.getHpChange(owner);
        // 保留1点血
        context.addPassiveValue(owner, AlterType.HP, -value + 1);
//        timedDamageReport.add(time, owner.getId(), Hp.of(-value + 1));
        final ReviveParam param = passiveState.getParam(ReviveParam.class);
        for (String buff : param.getBuffs()) {
            BuffFactory.addBuff(buff, owner, owner, time, timedDamageReport, null);
        }
        passiveState.addCD(time);
    }
}
