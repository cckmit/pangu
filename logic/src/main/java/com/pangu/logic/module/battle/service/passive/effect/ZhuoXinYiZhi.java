package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.skill.effect.MpChange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ZhuoXinYiZhi implements AttackPassive {
    @Autowired
    private MpChange mpChange;

    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (!skillState.getTag().equals("zhuo_xin_yi_zhi")) {
            return;
        }
        final EffectState effectState = new EffectState(null, 0);
        effectState.setParamOverride(passiveState.getParam(Integer.class));
        mpChange.execute(effectState, owner, owner, skillReport, time, skillState, context);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.ZHUO_XIN_YI_ZHI;
    }
}
