package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.effect.MiDieHuaHaiBuff;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import org.springframework.stereotype.Component;

/**
 * 蝴蝶仙子·莉亚娜技能：迷蝶花海
 * 1级：在空中来回飞舞一次,撒下花粉,使敌方全体陷入睡眠,持续3秒睡眠结束时会造成睡眠时受到伤害的25%的伤害
 * 2级：持续时间提升至4秒
 * 3级：睡眠结束时伤害提升至睡眠时伤害的30%
 * 4级：持续时间提升至5秒
 * @author Kubby
 */
@Component
public class MiDieHuaHaiPassive implements DamagePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.MI_DIE_HUA_HAI;
    }

    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time,
                       Context context, SkillState skillState, SkillReport skillReport) {
        BuffState buffState = passiveState.getAddition(BuffState.class);
        MiDieHuaHaiBuff.MiDieHuaHaiBuffAddition buffAddition = buffState
                .getAddition(MiDieHuaHaiBuff.MiDieHuaHaiBuffAddition.class);
        buffAddition.incDamage(damage);
    }

}
