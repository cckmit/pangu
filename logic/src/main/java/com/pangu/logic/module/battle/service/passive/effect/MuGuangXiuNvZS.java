package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackBeforePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import org.springframework.stereotype.Component;

/**
 * 1：受到光之护佑的友军，后续8秒内，防御提升25%
 * 10：受到光之护佑的友军，后续8秒内，防御提升35%
 * 20：受到光之护佑的友军，后续8秒内，防御提升50%
 * 30：受到光之护佑的友军，后续8秒内，防御提升65%
 */
@Component
public class MuGuangXiuNvZS implements AttackBeforePassive {
    @Override
    public PassiveType getType() {
        return PassiveType.MU_GUANG_XIU_NV_ZS;
    }

    @Override
    public void attackBefore(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time, Context context, SkillReport skillReport) {
        if (!effectState.getId().startsWith("GUANG_ZHI_HU_YOU")) {
            return;
        }
        final String buffId = passiveState.getParam(String.class);
        BuffFactory.addBuff(buffId, owner, target, time, skillReport, null);
    }

    @Override
    public void attackEnd(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time, Context context, SkillReport skillReport) {

    }
}
