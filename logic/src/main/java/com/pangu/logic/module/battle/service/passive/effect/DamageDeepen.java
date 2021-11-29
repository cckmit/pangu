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
import com.pangu.logic.module.battle.service.passive.param.DamageDeepenParam;
import com.pangu.logic.utils.ExpressionHelper;
import com.pangu.framework.utils.math.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 在攻击时有15%的几率造成额外50%的伤害。
 */
@Component
public class DamageDeepen implements AttackPassive {
    @Override
    public PassiveType getType() {
        return PassiveType.DAMAGE_DEEPEN;
    }

    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (damage >= 0) {
            return;
        }
        doDmgDeepen(passiveState, owner, target, damage, time, context, skillState, skillReport);
    }

    public void doDmgDeepen(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        DamageDeepenParam param = passiveState.getParam(DamageDeepenParam.class);
        if (StringUtils.isNotEmpty(param.getExpression())) {
            final Map<String, Object> ctx = new HashMap<>();
            ctx.put("time", time);
            ctx.put("owner", owner);
            ctx.put("target", target);
            ctx.put("skillState", skillState);
            boolean hit = ExpressionHelper.invoke(param.getExpression(), boolean.class, ctx);
            if (!hit) {
                return;
            }
        } else {
            double rate = param.getRate();
            if (rate > 0 && !RandomUtils.isHit(rate)) {
                return;
            }
        }
        double deepenRate = param.getEnhanceValue();
        long deepen = (long) (deepenRate * damage);
        context.addPassiveValue(target, AlterType.HP, deepen);
        skillReport.add(time, target.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), Hp.of(deepen)));
    }
}
