package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.action.custom.FeiXingJiShiZSAction;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import org.springframework.stereotype.Component;

/**
 * 飞行技师·比佛利专属装备
 * 1：只要自己在3秒内没有受到伤害,则自己造成的伤害提升20%
 * 10：只要自己在3秒内没有受到伤害,则自己造成的伤害提升30%
 * 20：只要自己在3秒内没有受到伤害,则自己造成的伤害提升40%
 * 30：只要自己在3秒内没有受到伤害,则自己造成的伤害提升50%
 * @author Kubby
 */
@Component
public class FeiXingJiShiZSPassive implements DamagePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.FEI_XING_JI_SHI_ZS;
    }

    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time,
                       Context context, SkillState skillState, SkillReport skillReport) {
        FeiXingJiShiZSAction action = passiveState.getAddition(FeiXingJiShiZSAction.class);
        if (action == null) {
            return;
        }
        action.hurt(time);
    }

}
