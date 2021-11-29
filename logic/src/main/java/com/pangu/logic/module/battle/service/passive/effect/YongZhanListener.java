package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.FilterType;
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
 * 敌军死亡时，为全体友军添加BUFF
 */
@Component
public class YongZhanListener implements UnitDiePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.YONG_ZHAN_LISTENER;
    }

    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> dieUnits) {
        boolean enemyDead = false;
        for (Unit dieUnit : dieUnits) {
            if (!dieUnit.isFriend(owner)) {
                enemyDead = true;
                break;
            }
        }
        if (!enemyDead) {
            return;
        }

        for (Unit friend : FilterType.FRIEND.filter(owner, time)) {
            BuffFactory.addBuff(passiveState.getParam(String.class), owner, friend, time, damageReport, null);
        }
        passiveState.addCD(time);
    }
}
