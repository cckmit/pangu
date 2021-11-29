package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.LveShiZhiYaParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import com.pangu.logic.module.battle.service.skill.effect.BuffUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 1：每次攻击撕开目标的伤口，使其进入易损状态。受到比上次攻击更多的伤害。持续6秒。每层1.5%，最多6层，每个目标独立计算
 * 10：每层2%，最多6层
 * 20：满层后，目标的被暴击率提升15%
 * 30：对满层的敌人，自身对其造成的伤害提升15%
 */
@Component
public class LveShiZhiYaZS implements AttackPassive {
    @Autowired
    private BuffUpdate buffUpdate;

    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final LveShiZhiYaParam param = passiveState.getParam(LveShiZhiYaParam.class);

        //给目标添加易损层数
        final EffectState effectState = new EffectState(null, 0);
        effectState.setParamOverride(param.getCounter());
        buffUpdate.execute(effectState, owner, target, skillReport, time, skillState, context);

        //获取目标身上的计数器计数
        final BuffState counter = target.getBuffStateByTag("lve_shi_zhi_ya_counter");
        if (counter == null) {
            return;
        }
        final Integer curCount = counter.getAddition(Integer.class);

        //叠满后添加debuff、增伤
        if (curCount >= param.getMaxCount()) {
            BuffFactory.addBuff(param.getDeBuff(), owner, target, time, skillReport, null);
            final double dmgUpRate = param.getDmgUpRate();
            final long dmgUp = (long) (dmgUpRate * damage);
            PassiveUtils.hpUpdate(context, skillReport, target, dmgUp, time);
        }
    }

    @Override
    public PassiveType getType() {
        return PassiveType.LVE_SHI_ZHI_YA_ZS;
    }
}
