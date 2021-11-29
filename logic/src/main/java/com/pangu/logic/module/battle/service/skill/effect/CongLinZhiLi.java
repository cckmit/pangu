package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 丛林之力
 * 每隔11秒运用丛林之力强化自己的手掌使自己的后续3次普通攻击得到强化强化后的普通攻击拥有更大的攻击范围,井会造成120%攻击力的伤害
 * 2级:强化普通攻击的伤害提升至130%攻击力
 * 3级:强化普通攻击的伤害提升至140%攻击力
 * 4级:冷却时间降低为9秒
 *
 * 该技能效果通过循环执行为被动充能
 */
@Component
public class CongLinZhiLi implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.CONG_LIN_ZHI_LI;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        //充能后，被动可使用的次数
        final Integer chargeCount = state.getParam(Integer.class);
        final List<PassiveState> passiveStateByType = owner.getPassiveStateByType(PassiveType.CONG_LIN_ZHI_LI);

        //不存在该被动直接返回。
        if (CollectionUtils.isEmpty(passiveStateByType)) {
            return;
        }

        //存在该被动时才往被动状态中充能
        final PassiveState passiveState = passiveStateByType.get(0);
        passiveState.setAddition(chargeCount);
    }
}
