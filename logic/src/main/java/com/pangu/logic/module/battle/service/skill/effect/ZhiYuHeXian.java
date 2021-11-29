package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.ZhiYuHeXianParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 律动和弦(2级)
 * 被动
 * 亨德尔的普通攻击变为向随机2名不同的英雄发射音符。音符命中友军时产生治疗效果，命中敌人时造成伤害
 * 。
 * 2级:将向3名不同的英雄发射音符
 */
@Component
public class ZhiYuHeXian implements SkillEffect {
    @Autowired
    private HpRecover hpRecover;
    @Autowired
    private HpMagicDamage hpMagicDamage;

    @Override
    public EffectType getType() {
        return EffectType.ZHI_YU_HE_XIAN;
    }
    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final ZhiYuHeXianParam param = state.getParam(ZhiYuHeXianParam.class);
        //命中友军则恢复
        if (owner.getFriend() == target.getFriend()) {
            state.setParamOverride(param.getRecover());
            hpRecover.execute(state, owner, target, skillReport, time, skillState, context);
        }
        //命中敌军则伤害
        else {
            state.setParamOverride(param.getDmg());
            hpMagicDamage.execute(state, owner, target, skillReport, time, skillState, context);
        }
        state.setParamOverride(null);
    }
}
