package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.resource.FightSkillSetting;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitHpChangePassive;
import com.pangu.logic.module.battle.service.passive.param.ConditionallyUpdateSkillParam;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import com.pangu.logic.utils.ExpressionHelper;
import com.pangu.framework.utils.reflect.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 自身生命值低于40%时，将会立刻获得一次“智慧絮语”的效果，每场战斗只能触发3次
 */
@Component
public class ConditionallyUpdateSkill implements UnitHpChangePassive {
    @Override
    public PassiveType getType() {
        return PassiveType.CONDITIONALLY_UPDATE_SKILL;
    }

    @Override
    public void hpChange(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> changeUnit) {
        final ConditionallyUpdateSkillParam param = passiveState.getParam(ConditionallyUpdateSkillParam.class);

        //  当前处于可打断状态
        if (!owner.cancelAction(time)) {
            return;
        }

        final Map<String, Object> ctx = new HashMap<>();
        ctx.put("owner", owner);
        if (!StringUtils.isEmpty(param.getTriggerCond()) && !ExpressionHelper.invoke(param.getTriggerCond(), boolean.class, ctx)) {
            return;
        }

        final String skillPrefix = param.getSkillPrefix();
        final String skillId = FightSkillSetting.toActiveSkillId(owner, skillPrefix);
        if (skillId == null) {
            return;
        }
        final SkillState skillState = SkillFactory.initState(skillId);
        if (param.isSpace()) {
            SkillFactory.updateSpace(time, owner, skillState);
        } else {
            SkillFactory.updateNextExecuteSkill(time, owner, skillState);
        }
    }
}
