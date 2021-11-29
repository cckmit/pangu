package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.AddPassive;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.utils.CalTypeHelper;
import com.pangu.logic.module.battle.service.buff.utils.CalValues;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveFactory;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.InitAddParam;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

//添加一些属性/被动/buff
@Component
public class InitAdd implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.INIT_ADD;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final InitAddParam param = state.getParam(InitAddParam.class);
        if (!verifyCondition(param, owner)) {
            return;
        }
        String[] passiveIds = param.getPassives();
        if (ArrayUtils.isNotEmpty(passiveIds)) {
            addPassives(owner, target, skillReport, time, passiveIds);
        }
        if (param.getCalType() != null && param.getAlters() != null) {
            addValues(owner, target, skillReport, time, context, param);
        }
        final String[] buffs = param.getBuffs();
        if (buffs != null) {
            for (String buffId : buffs) {
                BuffFactory.addBuff(buffId, owner, target, time, skillReport, null);
            }
        }
    }

    private boolean verifyCondition(InitAddParam param, Unit owner) {
        final Double hpCondition = param.getHpCondition();
        if (hpCondition == null) {
            return true;
        }
        if (owner.getCurrentHpRate() > hpCondition) {
            return true;
        }
        return false;
    }

    private void addValues(Unit owner, Unit target, SkillReport skillReport, int time, Context context, InitAddParam param) {
        CalValues calValues = CalTypeHelper.calValues(param.getCalType(), param.getAlters(), owner, target, param.getFactor());
        final Map<AlterType, Number> values = calValues.getValues();
        String targetId = target.getId();
        for (Map.Entry<AlterType, Number> entry : values.entrySet()) {
            AlterType alterType = entry.getKey();
            Number number = entry.getValue();
            skillReport.add(time, targetId, new UnitValues(alterType, number));
            context.addValue(target, alterType, number);
        }
    }

    private void addPassives(Unit owner, Unit target, SkillReport skillReport, int time, String[] passiveIds) {
        Map<String, PassiveState> passiveStatesById = target.getPassiveStatesById();
        for (String passiveId : passiveIds) {
            if (passiveStatesById.containsKey(passiveId)) {
                continue;
            }
            PassiveState passiveState = PassiveFactory.initState(passiveId, time);
            target.addPassive(passiveState, owner);
            skillReport.add(time, target.getId(), new AddPassive(passiveId));
        }
    }
}
