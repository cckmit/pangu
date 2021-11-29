package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.param.AddEpOnSkillReleaseParam;
import org.springframework.stereotype.Component;

/**
 * 释放指定技能增加能量
 */
@Component
public class AddEpOnSkillRelease implements SkillReleasePassive {
    @Override
    public PassiveType getType() {
        return PassiveType.ADD_EP_ON_SKILL_RELEASE;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time, Context context, SkillReport skillReport) {
        if (owner != attacker) {
            return;
        }
        final AddEpOnSkillReleaseParam param = passiveState.getParam(AddEpOnSkillReleaseParam.class);
        final Integer addEp = param.getTag2Ep().get(skillState.getTag());
        if (addEp == null) {
            return;
        }
        context.addPassiveValue(owner, AlterType.EP, addEp);
        skillReport.add(time, owner.getId(), new UnitValues(AlterType.EP, addEp));
    }
}
