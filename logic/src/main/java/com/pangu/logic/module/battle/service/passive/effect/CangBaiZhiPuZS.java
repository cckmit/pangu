package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.CangBaiZhiPuZSParam;
import com.pangu.logic.module.battle.service.skill.effect.BuffUpdate;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("PASSIVE:CangBaiZhiPuZS")
public class CangBaiZhiPuZS implements AttackPassive {
    @Autowired
    private BuffUpdate buffUpdate;
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (!context.isCrit(target)) {
            return;
        }
        final CangBaiZhiPuZSParam param = passiveState.getParam(CangBaiZhiPuZSParam.class);
        if (!RandomUtils.isHit(param.getTriggerRate())) {
            return;
        }
        final EffectState effectState = new EffectState(null, 0);
        effectState.setParamOverride(param.getBuff());
        buffUpdate.execute(effectState,owner,target,skillReport,time,skillState,context);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.CANG_BAI_ZHI_PU_ZS;
    }
}
