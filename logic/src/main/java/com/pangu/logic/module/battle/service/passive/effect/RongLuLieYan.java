package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.DispelType;
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
 * 熔炉烈焰
 * 坠星堡垒高速旋转自己的“破坏者”，产生并释放火焰，造成一次全场伤害，命中后移除目标身上全部增益BUFF，每移除一个BUFF降低敌人100能量
 */
@Component
public class RongLuLieYan implements AttackPassive {
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (skillState.getType() != SkillType.SKILL) {
            return;
        }

        final List<BuffState> usefulBuffs = target.getBuffByDispel(DispelType.USEFUL);
        if (CollectionUtils.isEmpty(usefulBuffs)) {
            return;
        }
        BuffFactory.removeBuffStates(usefulBuffs, target, time);

        //每移除一个增益状态，扣除一定的能量
        PassiveUtils.mpUpdate(context, skillReport, owner, target, (long) -passiveState.getParam(Integer.class) * usefulBuffs.size(), time, passiveState);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.RONG_LU_LIE_YAN;
    }
}
