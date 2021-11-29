package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.param.HuiMieDanMuParam;
import com.pangu.logic.module.battle.service.skill.effect.BuffUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class HuiMieDanMu implements SkillReleasePassive {
    @Autowired
    private BuffUpdate buffUpdate;

    @Override
    public PassiveType getType() {
        return PassiveType.HUI_MIE_DAN_MU;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time, Context context, SkillReport skillReport) {
        final HuiMieDanMuParam param = passiveState.getParam(HuiMieDanMuParam.class);
        if (!Arrays.asList(param.getSkillTypes()).contains(skillState.getType())) {
            return;
        }

        final EffectState effectState = new EffectState(null, 0);
        effectState.setParamOverride(param.getBuff());
        buffUpdate.execute(effectState, owner, owner, skillReport, time, skillState, context);
    }
}
