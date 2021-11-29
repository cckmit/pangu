package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import org.springframework.stereotype.Component;

/**
 * 流沙喷涌
 * 对一名敌人造成60%攻击力的伤害,并使该敌人在后续6秒内对角色的伤害下降40%,且每秒损失2.5%当前生命值转化为角色的生命值
 * 2级:持续时间提升至8秒
 * 3级:伤害提升至80%攻击力
 * 4级:伤害提升至100%攻击力
 *
 * 该被动仅用于降低目标对自身造成的伤害
 */
@Component
public class LiuShaPenYong implements DamagePassive {
    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final long hpChange = context.getHpChange(owner);
        if (hpChange >= 0) {
            return;
        }
        final BuffState deBuff = attacker.getBuffStateByTag("liu_sha_pen_yong_debuff");
        if (deBuff == null) {
            return;
        }
        final double dmgCutRate = passiveState.getParam(Double.class);
        final long dmgCut = -(long) (hpChange * dmgCutRate);
        PassiveUtils.hpUpdate(context, skillReport, owner, dmgCut, time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.LIU_SHA_PEN_YONG;
    }
}
