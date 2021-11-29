package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.HpPctDamageParam;
import com.pangu.logic.utils.ExpressionHelper;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 按血量最大值百分比算伤害
 * @author Kubby
 */
@Component
public class HpPctDamage implements SkillEffect {

    @Override
    public EffectType getType() {
        return EffectType.HP_PCT_DAMAGE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time,
                        SkillState skillState, Context context) {

        HpPctDamageParam param = state.getParam(HpPctDamageParam.class);

        double rate = param.getRate();

        long damage = -(long) (target.getValue(UnitValue.HP_MAX) * rate);

        if (!StringUtils.isBlank(param.getLimitExpr())) {
            ExprContext exprContext = ExprContext.of(owner, target);
            long maxDamage = -ExpressionHelper.invoke(param.getLimitExpr(), Long.class, exprContext);
            damage = Math.max(damage, maxDamage);
        }

        if (damage >= 0) {
            damage = -1;
        }

        context.addValue(target, AlterType.HP, damage);

        skillReport.add(time, target.getId(), Hp.of(damage));

    }

    @Getter
    public static class ExprContext {

        private Unit owner;

        private Unit target;

        public static ExprContext of(Unit owner, Unit target) {
            ExprContext context = new ExprContext();
            context.owner = owner;
            context.target = target;
            return context;
        }

    }
}
