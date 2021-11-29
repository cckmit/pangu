package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitDiePassive;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 根据阵亡队友数量回复能量
 */
@Component
public class MpRecoverPerFriendDie implements UnitDiePassive {
    @Override
    public PassiveType getType() {
        return PassiveType.MP_RECOVER_PER_FRIEND_DIE;
    }

    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> dieUnits) {
        int friendCount=0;
        for (Unit dieUnit : dieUnits) {
            if (dieUnit.getFriend()==owner.getFriend()) {
                friendCount++;
            }
        }
        if (friendCount<=0) {
            return;
        }
        final Integer mpRecoverPerFriendDie = passiveState.getParam(Integer.class);
        final int totalMpRecover = mpRecoverPerFriendDie * friendCount;
        PassiveUtils.mpUpdate(context,damageReport, owner, owner,totalMpRecover,time,passiveState);
    }
}
