package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.HeroRaceType;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.Immune;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitHpChangePassive;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 免疫指定种族以外的伤害
 */
@Component
public class DmgIgnoreExceptCertainRace implements UnitHpChangePassive {
    @Override
    public PassiveType getType() {
        return PassiveType.DMG_IGNORE_EXCEPT_CERTAIN_RACE;
    }

    @Override
    public void hpChange(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> changeUnit) {
        final HeroRaceType raceType = passiveState.getParam(HeroRaceType.class);

        //  只会受到来自特定种族的伤害
        if (attacker.getHeroRaceType() == raceType) {
            return;
        }

        //  本次结算周期间所受到的伤害
        final long actualHpChange = context.getActualHpChange(owner);
        final long curDmg = Math.min(0, context.getHpChange(owner));
        //  对冲数值
        final long offset = -curDmg - actualHpChange;

        //  被动持有者生命未减少，或被即死也无需处理
        if (offset <= 0 || owner.isDead()) {
            return;
        }

        context.addPassiveValue(owner, AlterType.HP, offset);
        final String ownerId = owner.getId();

        final PassiveValue pv = PassiveValue.of(passiveState.getId(), ownerId);
//        pv.add(Hp.of(offset));
        pv.add(new Immune());
        damageReport.add(time, ownerId, pv);
    }
}
