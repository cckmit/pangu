package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.OwnerDiePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.skill.effect.SummonSkill;
import com.pangu.logic.module.battle.service.skill.param.SummonSkillParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SummonOnUnitDie implements OwnerDiePassive {
    @Autowired
    private SummonSkill summonSkill;

    @Override
    public PassiveType getType() {
        return PassiveType.SUMMON_ON_OWNER_DIE;
    }

    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attack, ITimedDamageReport timedDamageReport, int time, Context context) {
        if (!owner.isDead()) {
            return;
        }
        final SummonSkillParam param = passiveState.getParam(SummonSkillParam.class);
        final EffectState effectState = new EffectState(null, 0);
        effectState.setParamOverride(param);
        summonSkill.doSummon(effectState, owner, time, context, timedDamageReport, PassiveValue.of(passiveState.getId(), owner.getId()));
    }
}
