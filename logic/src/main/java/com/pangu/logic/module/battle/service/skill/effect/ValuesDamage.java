package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.values.Mp;
import com.pangu.logic.module.battle.service.alter.MpAlter;
import com.pangu.logic.module.battle.service.skill.param.ValuesDamageParam;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.buff.utils.CalTypeHelper;
import com.pangu.logic.module.battle.service.buff.utils.CalValues;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 直接添加属性
 */
@Component
public class ValuesDamage implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.VALUES_DAMAGE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        ValuesDamageParam param = state.getParam(ValuesDamageParam.class);
        CalValues calValues = CalTypeHelper.calValues(param.getCalType(), param.getAlters(), owner, target, param.getFactor());
        Map<AlterType, Number> values = calValues.getValues();
        Number hpChange = values.remove(AlterType.HP);
        String targetId = target.getId();
        if (hpChange != null) {
            skillReport.add(time, targetId, Hp.of(hpChange.longValue()));
            context.addValue(target, AlterType.HP, hpChange);
        }
        final Number mpChange = values.remove(AlterType.MP);
        if (mpChange != null) {
            final long mpChangeForReport = MpAlter.calMpChange(target, mpChange.longValue());
            skillReport.add(time, targetId, new Mp(mpChangeForReport));
            context.addValue(target, AlterType.MP, mpChange);
        }
        for (Map.Entry<AlterType, Number> entry : values.entrySet()) {
            AlterType alterType = entry.getKey();
            Number number = entry.getValue();
            skillReport.add(time, targetId, new UnitValues(alterType, number));
            context.addValue(target, alterType, number);
        }
    }
}
