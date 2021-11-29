package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.BingFengShiJieParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 冰封世界
 * 释放时对全场敌人造成140%攻击力的伤害并使战场天气变为“冰雾”持续14秒。当处于“冰雾”天气时,敌入每秒受到40%攻击力的伤害且生命值低于30%的敌人将被冻结，直至其生命恢复至30%以上或冰雾天气结束
 * 2级:当处于冰雾天气时,敌人的生命恢复效果下降50%
 * 3级:“冰雾”天气持续时间增加至16秒
 * 4级:“冰雾”天气持续时间增加至18秒
 */
@Component
public class BingFengShiJie implements SkillEffect {
    @Autowired
    private HpMagicDamage magicDamage;

    @Override
    public EffectType getType() {
        return EffectType.BING_FENG_SHI_JIE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final BingFengShiJieParam param = state.getParam(BingFengShiJieParam.class);
        if (context.getLoopTimes() == 1) {
            BuffFactory.addBuff(param.getDeBuff(), owner, target, time, skillReport, null);
            state.setParamOverride(param.getReleaseDmg());
        } else {
            state.setParamOverride(param.getDotDmg());
        }
        magicDamage.execute(state, owner, target, skillReport, time, skillState, context);
        state.setParamOverride(null);
    }
}
