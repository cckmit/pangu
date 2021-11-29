package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitDiePassive;
import com.pangu.logic.module.battle.service.passive.param.AddStateOnKillingParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import com.pangu.framework.utils.reflect.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 使用“无尽杀戮的深渊”击杀敌方英雄后，对全场敌人造成2秒恐惧效果
 */
@Component
public class AddStateOnKilling implements UnitDiePassive {
    @Override
    public PassiveType getType() {
        return PassiveType.ADD_STATE_ON_KILLING;
    }

    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> dieUnits) {
        final AddStateOnKillingParam param = passiveState.getParam(AddStateOnKillingParam.class);
        final StateAddParam stateParam = param.getStateParam();
        if (stateParam == null) {
            return;
        }
        if (param.isSelfKilling() && owner != attacker) {
            return;
        }
        if (!StringUtils.isEmpty(param.getSkillTag())
                && context.getRootSkillEffectAction() != null
                && !context.getRootSkillEffectAction().getSkillState().getTag().equals(param.getSkillTag())) {
            return;
        }

        List<Unit> targets;
        if (param.getTarget() != null) {
            targets = TargetSelector.select(owner, param.getTarget(), time);
        } else {
            targets = Collections.singletonList(attacker);
        }
        for (Unit target : targets) {
            PassiveUtils.addState(owner, target, stateParam.getState(), stateParam.getTime() + time, time, passiveState, context, damageReport);
        }
    }
}
