package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.PositionChange;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.ctx.OwnerTargetCtx;
import com.pangu.logic.module.battle.service.skill.param.YanMieMoYanParam;
import com.pangu.logic.utils.ExpressionHelper;
import com.pangu.framework.utils.math.RandomUtils;
import com.pangu.framework.utils.reflect.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 攻击敌方能量最高的单位，造成120%伤害
 * 有50%概率额外造成目标最大生命值20%的伤害；
 * 且有30%的概率击退目标，若成功击退目标，则降低其40%的怒气回复速度，若成功击退目标，则使我方当前攻击最高的单位回复200点怒气
 */
@Component
public class YanMieMoYan implements SkillEffect {
    @Autowired
    private HpHigherDamage hpHigherDamage;

    @Autowired
    private Repel repel;

    @Autowired
    private MpChange mpChange;


    @Override
    public EffectType getType() {
        return EffectType.YAN_MIE_MO_YAN;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final YanMieMoYanParam param = state.getParam(YanMieMoYanParam.class);

        //  造成主要伤害
        state.setParamOverride(param.getDamageParam());
        hpHigherDamage.execute(state, owner, target, skillReport, time, skillState, context);
        state.setParamOverride(null);

        //  造成额外伤害
        final String addDmgExp = param.getAddDmgExp();
        if (!StringUtils.isEmpty(addDmgExp) && RandomUtils.isHit(param.getAddDmgTriggerRate())) {
            OwnerTargetCtx ctx = new OwnerTargetCtx(time, owner, target);
            Number ret = ExpressionHelper.invoke(addDmgExp, Number.class, ctx);
            long damage = ret.longValue();
            if (damage < 0) {
                context.addValue(target, AlterType.HP, damage);
                skillReport.add(time, target.getId(), Hp.of(damage));
            }
        }

        //  击退
        if (!RandomUtils.isHit(param.getOtherBonusTriggerRate())) {
            return;
        }
        final PositionChange positionReport = new PositionChange();
        final boolean repelSuccess = repel.doRepel(param.getRepelDistance(), owner, target, time, positionReport);
        if (!repelSuccess) {
            return;
        }
        //  击退成功则添加debuff
        skillReport.add(time, target.getId(), positionReport);
        BuffFactory.addBuff(param.getDeBuff(), owner, target, time, skillReport, null);

        //  并回复能量
        state.setParamOverride(param.getMpChange());
        mpChange.execute(state, owner, target, skillReport, time, skillState, context);
        state.setParamOverride(null);
    }
}
