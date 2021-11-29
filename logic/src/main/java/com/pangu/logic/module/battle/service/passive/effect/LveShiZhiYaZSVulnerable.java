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
 * 1：每次攻击撕开目标的伤口，使其进入易损状态。受到比上次攻击更多的伤害。持续6秒。每层1.5%，最多6层，每个目标独立计算
 * 10：每层2%，最多6层
 * 20：满层后，目标的被暴击率提升15%
 * 30：对满层的敌人，自身对其造成的伤害提升15%"
 *
 * 该被动添加到处于易损状态的目标身上，用于在每次受伤时加深伤害。该被动的生命周期由添加该被动的buff决定。
 */
@Component
public class LveShiZhiYaZSVulnerable implements DamagePassive {
    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final long hpChange = context.getHpChange(owner);
        if (hpChange>=0) {
            return;
        }
        final Double dmgUpRatePerCount = passiveState.getParam(Double.class);

        //获取易损层数并计算实际增伤比例
        final BuffState counter = owner.getBuffStateByTag("lve_shi_zhi_ya_counter");
        if (counter == null) {
            return;
        }
        final Integer curCount = counter.getAddition(Integer.class);
        final double totalDmgUpRate = dmgUpRatePerCount * curCount;
        final long dmgUp = (long) (totalDmgUpRate * damage);
        PassiveUtils.hpUpdate(context,skillReport, owner,dmgUp,time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.LVE_SHI_ZHI_YA_ZS_VULNERABLE;
    }
}
