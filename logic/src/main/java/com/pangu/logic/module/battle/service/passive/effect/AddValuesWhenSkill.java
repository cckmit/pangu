package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.buff.utils.CalTypeHelper;
import com.pangu.logic.module.battle.service.buff.utils.CalValues;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.param.AddValuesWhenSkillParam;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 释放技能时添加属性
 * @author Kubby
 */
@Component
public class AddValuesWhenSkill implements SkillReleasePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.ADD_VALUES_WHEN_SKILL;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time,
                             Context context, SkillReport skillReport) {
        if (owner != attacker) {
            return;
        }
        AddValuesWhenSkillParam param = passiveState.getParam(AddValuesWhenSkillParam.class);
        if (param.getTypes() != null && !param.getTypes().contains(skillState.getType())) {
            return;
        }

        CalValues calValues = CalTypeHelper
                .calValues(param.getCalType(), param.getAlters(), owner, owner, param.getFactor());
        for (Map.Entry<AlterType, Number> entry : calValues.getValues().entrySet()) {
            AlterType alterType = entry.getKey();
            Number number = entry.getValue();
            skillReport.add(time, owner.getId(),
                    PassiveValue.single(passiveState.getId(), owner.getId(), new UnitValues(alterType, number)));
            context.addPassiveValue(owner, entry.getKey(), entry.getValue());
        }

        passiveState.addCD(time);
    }
}
