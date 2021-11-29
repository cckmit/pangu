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
import com.pangu.logic.module.battle.service.passive.param.VulnerableParam;
import org.springframework.stereotype.Component;

@Component
public class Vulnerable implements DamagePassive {
    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (damage >= 0) return;
        final VulnerableParam param = passiveState.getParam(VulnerableParam.class);
        final double damageRate = param.getAddRate();
        final long additionalDmg = (long) (damage * damageRate);
        context.addPassiveValue(owner, AlterType.HP, additionalDmg);
        skillReport.add(time, owner.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), Hp.of(additionalDmg)));
    }

    @Override
    public PassiveType getType() {
        return PassiveType.VULNERABLE;
    }
}
