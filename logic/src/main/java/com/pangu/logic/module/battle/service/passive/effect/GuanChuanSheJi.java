package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 贯穿射击:
 * 每10秒进行一次穿透射击,造成150%物理攻击伤害(目标身后的单位受到20%的伤害)
 * 2级:伤害提升至200%
 * 3级:触发时间降低至7秒
 * 4级:身后的单位受到的伤害提升至50%
 *
 * 此被动用于执行伤害衰减
 */
@Component
public class GuanChuanSheJi implements AttackPassive {
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (damage>=0) {
            return;
        }

        final List<Unit> targets = context.getRootSkillEffectAction().getTargets();
        if (CollectionUtils.isEmpty(targets)) {
            return;
        }

        //首个单位伤害不衰减
        if (target == targets.get(0)) {
            return;
        }

        final Double dmgCutTo = passiveState.getAddition(Double.class);
        final long dmgChange = -(long) (damage * (1 - dmgCutTo));
        PassiveUtils.hpUpdate(context,skillReport,target,dmgChange,time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.GUAN_CHUAN_SHE_JI;
    }
}
