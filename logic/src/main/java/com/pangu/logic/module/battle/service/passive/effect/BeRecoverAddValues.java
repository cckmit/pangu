package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.buff.param.DefaultAddValueParam;
import com.pangu.logic.module.battle.service.buff.utils.CalTypeHelper;
import com.pangu.logic.module.battle.service.buff.utils.CalValues;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.RecoverPassive;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 被友军治疗时给自己添加属性
 */
@Component
public class BeRecoverAddValues implements RecoverPassive {
    @Override
    public void recover(PassiveState passiveState, Unit owner, Unit from, long recover, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (owner == from) {
            return;
        }
        if (owner.getFriend() != from.getFriend()) {
            return;
        }
        final DefaultAddValueParam param = passiveState.getParam(DefaultAddValueParam.class);
        final CalValues calValues = CalTypeHelper.calValues(param.getCalType(), param.getAlters(), owner, from, param.getFactor());
        for (Map.Entry<AlterType, Number> entry : calValues.getValues().entrySet()) {
            skillReport.add(time, owner.getId(), new UnitValues(entry.getKey(), entry.getValue()));
            context.addPassiveValue(owner, entry.getKey(), entry.getValue());
        }
        passiveState.addCD(time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.BE_RECOVER_ADD_VALUES;
    }
}
