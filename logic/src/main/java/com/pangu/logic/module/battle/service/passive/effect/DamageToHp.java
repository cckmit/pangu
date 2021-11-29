package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import org.springframework.stereotype.Component;

/**
 * 将攻击全部转化为生命
 */
@Component
public class DamageToHp implements DamagePassive {
    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        Double param = passiveState.getParam(Double.class);
        if (param == null) {
            param = 0.5;
        }
        final long hpChange = context.getHpChange(owner);
        final long recover = (long) (-hpChange * param);
        long addHp = -hpChange + recover;
        PassiveValue passiveValue = PassiveValue.single(passiveState.getId(), owner.getId(), Hp.fromRecover(recover));
        skillReport.add(time, owner.getId(), passiveValue);

        context.addPassiveValue(owner, AlterType.HP, addHp);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.DAMAGE_TO_HP;
    }
}
