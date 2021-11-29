package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillSelectPassive;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import org.springframework.stereotype.Component;

/**
 * 丛林之力
 * 每隔11秒运用丛林之力强化自己的手掌使自己的后续3次普通攻击得到强化强化后的普通攻击拥有更大的攻击范围,井会造成120%攻击力的伤害
 * 2级:强化普通攻击的伤害提升至130%攻击力
 * 3级:强化普通攻击的伤害提升至140%攻击力
 * 4级:冷却时间降低为9秒
 */
@Component("PASSIVE:CongLinZhiLi")
public class CongLinZhiLi implements SkillSelectPassive {
    @Override
    public PassiveType getType() {
        return PassiveType.CONG_LIN_ZHI_LI;
    }

    @Override
    public SkillState skillSelect(PassiveState passiveState, SkillState skillState, Unit owner, int time) {
        if (skillState.getType() != SkillType.NORMAL) {
            return null;
        }

        //获取由主动技能往该被动的充能
        Integer chargeCount = passiveState.getAddition(Integer.class);
        if (chargeCount == null) {
            return null;
        }
        chargeCount--;
        passiveState.setAddition(chargeCount);
        if (chargeCount < 0) {
            return null;
        }
        return SkillFactory.initState(passiveState.getParam(String.class));
    }
}
