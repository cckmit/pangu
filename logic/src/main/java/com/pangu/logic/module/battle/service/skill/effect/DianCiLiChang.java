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
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import com.pangu.logic.module.battle.service.skill.param.DianCiLiChangParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 电磁力场
 * 全场释放大量电磁,对附近的敌入持续造成每秒60%魔法伤害,持续8秒持续时间结束后,期间造成的总伤害的25%将转化为自身的生命恢复
 * 2级:伤害提升至75%,总伤害的30%将变为自身的恢复
 * 3级:持续时间提升至10秒
 * 4级:伤害提升至90%攻击力,25%的治疗将提前到造成伤害时就产生效果
 */
@Component
public class DianCiLiChang implements SkillEffect {
    @Autowired
    private HpMagicDamage hpMagicDamage;
    @Autowired
    private SuckHP suckHP;

    @Override
    public EffectType getType() {
        return EffectType.DIAN_CI_LI_CHANG;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final DianCiLiChangParam param = state.getParam(DianCiLiChangParam.class);
        //构造伤害效果上下文
        state.setParamOverride(new DamageParam(param.getFactor()));
        hpMagicDamage.execute(state, owner, target, skillReport, time, skillState, context);
        state.setParamOverride(null);

        //提前生效的治疗
        state.setParamOverride(param.getDamageToCureRate() * param.getHungryCureRate());
        suckHP.execute(state, owner, target, skillReport, time, skillState, context);
        state.setParamOverride(null);

        //技能彻底释放结束后的治疗
        final Addition addition = getAddition(state);
        //每次skillEffectAction被执行时都会重置context，其中的LoopTimes是通过skillEffectAction维护，
        //每次skillEffectAction执行时赋值到新的context中
        if (context.getLoopTimes() == skillState.getExecuteTimes()) {
            //伤害执行完毕，执行回血
            long damage = addition.accDmg + context.getHpChange(target);
            if (damage >= 0) {
                return;
            }
            double rate = param.getDamageToCureRate() * (1 - param.getHungryCureRate());
            long value = (long) (-rate * damage);
            // 将值变更作用到上下文
            context.addValue(owner, AlterType.HP, value);
            skillReport.add(time, owner.getId(), Hp.of(value));
            //累计伤害清零
            addition.accDmg = 0L;
        } else {
            //伤害未执行完毕，累计伤害
            addition.accDmg += context.getHpChange(target);
        }
    }

    private Addition getAddition(EffectState effectState) {
        Addition addition = effectState.getAddition(Addition.class);
        if (addition == null) {
            addition = new Addition();
            //不要忘记初始化之后绑定到effectState中
            effectState.setAddition(addition);
        }
        return addition;
    }

    private static class Addition {
        //累计伤害
        private long accDmg;
    }
}
