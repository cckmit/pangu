package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.MiZongZhiDieParam;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import org.springframework.stereotype.Component;

/**
 * 蝴蝶仙子·莉亚娜技能：迷踪之蝶
 * 1级：受到伤害时,解除自身受到的控制效果,并瞬移至随机目标背后瞬移期间不会被攻击,瞬移后使附近友军攻速提升10%,持续3秒；对附近敌人造成120%攻击力的伤害。冷却时间9秒
 * 2级：每次受到伤害时,降低冷却时间0.5秒
 * 3级：伤害提升至135%攻击力
 * 4级：攻速提升15%
 * @author Kubby
 */
@Component
@Deprecated
public class MiZongZhiDie implements DamagePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.MI_ZONG_ZHI_DIE;
    }

    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time,
                       Context context, SkillState skillState, SkillReport skillReport) {
        MiZongZhiDieParam param = passiveState.getParam(MiZongZhiDieParam.class);

        int nextTime = passiveState.getAddition(int.class, -1);
        if (nextTime > 0 && time <= nextTime) {
            
            passiveState.setAddition(nextTime - param.getPreCdDec());
            return;
        }

        SkillFactory.updateNextExecuteSkill(time, owner, param.getSkillId());

        
        passiveState.setAddition(time + param.getCd());
    }

}
