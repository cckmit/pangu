package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.ConditionallyDmgUpParam;
import com.pangu.logic.utils.ExpressionHelper;
import com.pangu.framework.utils.reflect.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * 无声突袭造成伤害如果暴击的话，伤害额外提升100%
 */
@Component
public class ConditionallyDmgUp implements AttackPassive {
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final ConditionallyDmgUpParam param = passiveState.getParam(ConditionallyDmgUpParam.class);
        final String triggerExp = param.getTriggerExp();
        final HashMap<String, Object> ctx = new HashMap(6, 1);
        ctx.put("owner", owner);
        ctx.put("target", target);
        ctx.put("skillState", skillState);
        ctx.put("context", context);
        ctx.put("passiveState", passiveState);
        if (!StringUtils.isEmpty(triggerExp)
                && !ExpressionHelper.invoke(triggerExp, boolean.class, ctx)) {
            return;
        }

        final double dmgUpRate = param.getDmgUpRate();
        long dmgChange;
        if (dmgUpRate != 0) {
            dmgChange = (long) (dmgUpRate * damage);
        } else {
            dmgChange = ExpressionHelper.invoke(param.getDmgExp(), Number.class, ctx).longValue();
        }
        context.addPassiveValue(owner, AlterType.HP, dmgChange);
        skillReport.add(time, target.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), Hp.of(dmgChange)));
    }

    @Override
    public PassiveType getType() {
        return PassiveType.CONDITIONALLY_DMG_UP;
    }
}
