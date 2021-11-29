package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.*;
import com.pangu.logic.module.battle.service.passive.param.HuoLiYaZhiParam;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import com.pangu.logic.module.battle.service.skill.effect.Repel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Deprecated
public class HuoLiYaZhi implements SkillSelectPassive, SkillReleasePassive, AttackPassive {
    @Autowired
    private Repel repel;

    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (skillState.getType() != SkillType.NORMAL) {
            return;
        }
        final HuoLiYaZhiParam param = passiveState.getParam(HuoLiYaZhiParam.class);
        final EffectState effectState = new EffectState(null, 0);
        effectState.setParamOverride(param.getRepelDist());
        repel.execute(effectState,owner,target,skillReport,time,skillState,context);
        BuffFactory.addBuff(param.getBuffId(), owner, target, time, skillReport, null);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.HUO_LI_YA_ZHI;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time, Context context, SkillReport skillReport) {
        if (skillState.getType() != SkillType.NORMAL) {
            return;
        }
        final Addition addition = getAddition(passiveState);
        addition.count++;
        if (addition.count > passiveState.getParam(HuoLiYaZhiParam.class).getTriggerCount()) {
            addition.count = 1;
        }
    }

    @Override
    public SkillState skillSelect(PassiveState passiveState, SkillState skillState, Unit owner, int time) {
        if (skillState.getType() != SkillType.NORMAL) {
            return null;
        }
        return SkillFactory.initState(passiveState.getParam(HuoLiYaZhiParam.class).getReplaceSkill());
    }

    private Addition getAddition(PassiveState passiveState) {
        Addition addition = passiveState.getAddition(Addition.class);
        if (addition == null) {
            addition = new Addition();
            passiveState.setAddition(addition);
        }
        return addition;
    }

    private static class Addition {
        private int count;
    }
}
