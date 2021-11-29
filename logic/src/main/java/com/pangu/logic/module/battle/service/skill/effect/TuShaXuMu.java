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
import com.pangu.logic.module.battle.service.skill.ctx.OwnerTargetCtx;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import com.pangu.logic.module.battle.service.skill.param.TuShaXuMuParam;
import com.pangu.logic.module.battle.service.skill.utils.SkillUtils;
import com.pangu.logic.utils.ExpressionHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 屠杀序幕:
 * 使近身的一名敌人倒地,持续2秒,期间造成多次35%攻击力的伤害(多次伤害由前端进行表现，服务端实际只做)
 * 2级:该技能的第五次伤害附加目标当前生命值15%的额外伤害
 * 3级:该技能的第五次伤害附加目标当前生命值20%的额外伤害
 * 4级:该技能的第五次伤害附加目标当前生命值25%的额外伤害
 */
@Component
public class TuShaXuMu implements SkillEffect {

    @Autowired
    private HpPhysicsDamage physicsDamage;

    @Override
    public EffectType getType() {
        return EffectType.TU_SHA_XU_MU;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        TuShaXuMuParam param = state.getParam(TuShaXuMuParam.class);
        DamageParam damageParam = new DamageParam(param.getFactor());
        state.setParamOverride(damageParam);
        physicsDamage.execute(state, owner, target, skillReport, time, skillState, context);
        state.setParamOverride(null);
        SkillUtils.addState(owner, target, param.getUnitState(), time, time + param.getStateDuration(), skillReport, context);

        String lastDamageExp = param.getLastDamageExp();
        if (StringUtils.isEmpty(lastDamageExp)) {
            return;
        }
        OwnerTargetCtx ctx = new OwnerTargetCtx(time, owner, target);
        Number ret = ExpressionHelper.invoke(lastDamageExp, Number.class, ctx);
        long damage = ret.longValue();

        if (damage < 0) {
            context.addValue(target, AlterType.HP, damage);
            skillReport.add(time, target.getId(), Hp.of(damage));
        }
    }
}
