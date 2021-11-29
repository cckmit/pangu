package com.pangu.logic.module.battle.service.passive;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.utils.ExpressionHelper;
import com.pangu.framework.utils.reflect.StringUtils;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 执行攻击方伤害对方的被动
 */
public interface AttackPassive extends Passive {

    /**
     * 执行攻击他人并造成伤害
     *
     * @param passiveState
     * @param owner
     * @param target
     * @param damage
     * @param time
     * @param context
     * @param skillState
     */
    void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport);

    default boolean atkPassiveVerify(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, String condExpr) {
        if (StringUtils.isEmpty(condExpr)) {
            return true;
        }
        final HashMap<String, Object> ctx = new HashMap<>();
        ctx.put("time", time);
        ctx.put("owner", owner);
        ctx.put("target", target);
        ctx.put("context", context);
        ctx.put("skillState", skillState);
        ctx.put("passiveState", passiveState);
        ctx.put("damage", damage);
        ctx.put("random", ThreadLocalRandom.current());

        if (ExpressionHelper.invoke(condExpr, Boolean.class, ctx)) {
            return true;
        }
        return false;
    }
}
