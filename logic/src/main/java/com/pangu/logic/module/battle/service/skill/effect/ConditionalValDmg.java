package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.ConditionalValDmgParam;
import com.pangu.logic.utils.ExpressionHelper;
import com.pangu.framework.utils.reflect.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.pangu.logic.module.battle.model.EffectType.CONDITIONAL_VAL_DMG;

/**
 * 带条件判断的属性修改效果
 */
@Component
public class ConditionalValDmg implements SkillEffect {
    @Autowired
    private ValuesDamage valuesDamage;

    @Override
    public EffectType getType() {
        return CONDITIONAL_VAL_DMG;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final ConditionalValDmgParam param = state.getParam(ConditionalValDmgParam.class);
        final String conditionExp = param.getConditionExp();
        final HashMap<String, Object> ctx = new HashMap<>();
        ctx.put("owner", owner);
        ctx.put("target", target);
        ctx.put("skillState", skillState);
        ctx.put("effectState", state);
        if (!StringUtils.isEmpty(conditionExp) && !ExpressionHelper.invoke(conditionExp, boolean.class, ctx)) {
            return;
        }

        //  可重复生效校验
        final Map<String, Object> addition = state.getAddition(Map.class, new HashMap());
        final Set<Unit> hitUnits = (Set<Unit>) addition.computeIfAbsent("hitUnits", k -> new HashSet<Unit>());
        if (param.isUnRepeatableOnSameTarget() && hitUnits.contains(target)) {
            return;
        }

        valuesDamage.execute(state, owner, target, skillReport, time, skillState, context);
        hitUnits.add(target);
    }
}
