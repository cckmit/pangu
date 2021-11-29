package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.action.SkillEffectAction;
import com.pangu.logic.module.battle.service.buff.utils.CalTypeHelper;
import com.pangu.logic.module.battle.service.buff.utils.CalValues;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitDiePassive;
import com.pangu.logic.module.battle.service.passive.param.KillAddValuesParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Set;

/**
 * 击杀单元后添加属性
 */
@Component
public class KillAddValues implements UnitDiePassive {
    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport skillReport, Set<Unit> dieUnits) {
        final KillAddValuesParam param = passiveState.getParam(KillAddValuesParam.class);

        //校验是否特定技能击杀
        final String triggerSkillTag = param.getTriggerSkillTag();
        if (!StringUtils.isEmpty(triggerSkillTag)) {
            final SkillEffectAction rootSkillEffectAction = context.getRootSkillEffectAction();
            if (rootSkillEffectAction == null) {
                return;
            }
            final SkillState skillState = rootSkillEffectAction.getSkillState();
            if (!triggerSkillTag.equals(skillState.getTag())) {
                return;
            }
        }

        if (owner != attacker && param.isNeedSelf()) {
            return;
        }
        boolean add = false;
        for (Unit dieUnit : dieUnits) {
            if (dieUnit.isSummon()) {
                Map<AlterType, String> summonAlters = param.getSummonAlters();
                if (summonAlters == null || summonAlters.isEmpty()) {
                    continue;
                }
                CalValues calValues = CalTypeHelper.calValues(param.getCalType(), param.getSummonAlters(), owner, dieUnit, param.getFactor());
                calValues(passiveState, owner, time, context, skillReport, calValues);
                add = true;

                continue;
            }
            CalValues calValues = CalTypeHelper.calValues(param.getCalType(), param.getAlters(), owner, dieUnit, param.getFactor());
            calValues(passiveState, owner, time, context, skillReport, calValues);
            add = true;
        }
        if (add) {
            passiveState.addCD(time);
        }
    }

    private void calValues(PassiveState passiveState, Unit owner, int time, Context context, ITimedDamageReport skillReport, CalValues calValues) {
        for (Map.Entry<AlterType, Number> entry : calValues.getValues().entrySet()) {
            AlterType alterType = entry.getKey();
            Number number = entry.getValue();
            skillReport.add(time, owner.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), new UnitValues(alterType, number)));
            context.addPassiveValue(owner, entry.getKey(), entry.getValue());
        }
    }

    @Override
    public PassiveType getType() {
        return PassiveType.KILL_ADD_VALUES;
    }
}
