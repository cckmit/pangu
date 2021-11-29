package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.utils.ExpressionHelper;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class ExprDamage implements SkillEffect {

    @Override
    public EffectType getType() {
        return EffectType.EXPR_DAMAGE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time,
                        SkillState skillState, Context context) {
        String expr = state.getParam(String.class);
        ExprDamageContext exprCtx = ExprDamageContext.of(owner, target);
        long damage = ExpressionHelper.invoke(expr, Long.class, exprCtx);
        context.addValue(target, AlterType.HP, damage);
        skillReport.add(time, target.getId(), Hp.of(damage));
    }

    @Getter
    public static class ExprDamageContext {

        private Unit owner;

        private Unit target;

        public static ExprDamageContext of(Unit owner, Unit target) {
            ExprDamageContext result = new ExprDamageContext();
            result.owner = owner;
            result.target = target;
            return result;
        }
    }

}