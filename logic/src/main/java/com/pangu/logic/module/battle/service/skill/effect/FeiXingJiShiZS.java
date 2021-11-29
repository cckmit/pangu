package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.action.custom.FeiXingJiShiZSAction;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.FeiXingJiShiZSParam;
import org.springframework.stereotype.Component;

/**
 * 飞行技师·比佛利专属装备
 * 1：只要自己在3秒内没有受到伤害,则自己造成的伤害提升20%
 * 10：只要自己在3秒内没有受到伤害,则自己造成的伤害提升30%
 * 20：只要自己在3秒内没有受到伤害,则自己造成的伤害提升40%
 * 30：只要自己在3秒内没有受到伤害,则自己造成的伤害提升50%
 *
 * @author Kubby
 */
@Component
public class FeiXingJiShiZS implements SkillEffect {

    @Override
    public EffectType getType() {
        return EffectType.FEI_XING_JI_SHI_ZS;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time,
                        SkillState skillState, Context context) {
        FeiXingJiShiZSParam param = state.getParam(FeiXingJiShiZSParam.class);
        PassiveState passiveState = owner.getPassiveStateByType(PassiveType.FEI_XING_JI_SHI_ZS).get(0);

        FeiXingJiShiZSAction action = FeiXingJiShiZSAction
                .of(owner, param.getBuffId(), param.getInterval(), time + 1000, skillReport);
        owner.getBattle().addWorldAction(action);

        passiveState.setAddition(action);
    }
}
