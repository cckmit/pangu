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
import com.pangu.logic.module.battle.service.skill.param.AnYingKuangBaoParam;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 暗影狂暴
 * 对最虚弱的敌人造成120%的伤害
 * 2级:攻击的伤害提升至200%
 * 3级:每次成功施法会使后续暗影狂暴的伤害提高10%，最多叠加3次，持续15秒
 * 4级:每次成功施法使后续死亡一指的伤害提高15%。
 */
@Component
public class AnYingKuangBao implements SkillEffect {

    @Autowired
    private HpMagicDamage magicDamageEffect;

    @Override
    public EffectType getType() {
        return EffectType.AN_YING_KUANG_BAO;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        AnYingKuangBaoParam param = state.getParam(AnYingKuangBaoParam.class);
        DamageParam damageParam = new DamageParam(param.getFactor());
        state.setParamOverride(damageParam);
        magicDamageEffect.execute(state, owner, target, skillReport, time, skillState, context);
        state.setParamOverride(null);
        long damage = context.getHpChange(target);
        if (damage >= 0) {
            return;
        }
        double damageEnhanceRate = param.getDamageEnhanceRate();
        if (damageEnhanceRate <= 0) {
            return;
        }
        AnYingKuangBaoValue addition = state.getAddition(AnYingKuangBaoValue.class);
        if (addition == null) {
            addition = new AnYingKuangBaoValue();
            state.setAddition(addition);
        }

        int continueTime = param.getContinueTime();
        int preValidTime = addition.validTime;
        addition.validTime = continueTime + time;

        // 第一次释放，在cd有效期，则叠加一层
        if (preValidTime > time) {
            int timesLimit = param.getTimesLimit();
            if (addition.repeatTimes < timesLimit) {
                ++addition.repeatTimes;
            }
        } else {
            addition.repeatTimes = 1;
        }

        long enhance = (long) (damageEnhanceRate * addition.repeatTimes * damage);
        context.addValue(target, AlterType.HP, enhance);
        skillReport.add(time, target.getId(), Hp.of(enhance));
    }

    private static class AnYingKuangBaoValue {
        // CD持续时间
        int validTime;
        // 当前叠加次数
        int repeatTimes;
    }
}
