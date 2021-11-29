package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import org.springframework.stereotype.Component;

/**
 * 召唤物攻击的伤害20%给召唤者
 */
@Component
public class SummonHp implements AttackPassive {
    @Override
    public PassiveType getType() {
        return PassiveType.SUMMON_HP;
    }

    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        Unit summonUnit = owner.getSummonUnit();
        if (summonUnit == null || summonUnit.isDead()) {
            return;
        }
        if (damage >= 0) {
            return;
        }
        Double rate = passiveState.getParam(Double.class);
        long hp = (long) (-damage * rate);
        if (hp <= 0) {
            return;
        }
        context.addPassiveValue(summonUnit, AlterType.HP, hp);
        skillReport.add(time, summonUnit.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), Hp.of(hp)));
    }
}
