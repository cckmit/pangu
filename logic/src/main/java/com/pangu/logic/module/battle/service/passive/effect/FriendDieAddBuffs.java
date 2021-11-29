package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitDiePassive;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 友方死亡后给自己添加一个BUFF
 */
@Component
public class FriendDieAddBuffs implements UnitDiePassive {
    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> dieUnits) {
        boolean add = false;
        for (Unit dieUnit : dieUnits) {
            if (owner.getFriend() != dieUnit.getFriend()) {
                continue;
            }
            final String[] param = passiveState.getParam(String[].class);
            for (String s : param) {
                BuffFactory.addBuff(s, owner, owner, time, damageReport, null);
            }
            add = true;
        }
        if (add) {
            passiveState.addCD(time);
        }
    }

    @Override
    public PassiveType getType() {
        return PassiveType.FRIEND_DIE_ADD_BUFFS;
    }
}
