package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.RecoverTargetPassive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 治疗量加深。复用伤害加深逻辑
 */
@Component
public class RecDeepen implements RecoverTargetPassive {
    @Autowired
    private DamageDeepen damageDeepen;

    @Override
    public PassiveType getType() {
        return PassiveType.REC_DEEPEN;
    }

    @Override
    public void recoverTarget(PassiveState passiveState, Unit owner, Unit target, long recover, int time, Context context, SkillState skillState, SkillReport skillReport) {
        damageDeepen.doDmgDeepen(passiveState, owner, target, recover, time, context, skillState, skillReport);
    }
}
