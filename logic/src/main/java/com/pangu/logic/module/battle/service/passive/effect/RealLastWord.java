package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.OwnerDiePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 执行阶段更晚的亡语被动
 */
@Component
public class RealLastWord implements OwnerDiePassive {
    @Autowired
    private LastWord lastWord;

    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attack, ITimedDamageReport timedDamageReport, int time, Context context) {
        if (!owner.isDead()) {
            return;
        }
        lastWord.die(passiveState, owner, attack, timedDamageReport, time, context);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.REAL_LAST_WORD;
    }
}
