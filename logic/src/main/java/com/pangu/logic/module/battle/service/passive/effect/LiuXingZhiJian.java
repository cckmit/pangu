package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import org.springframework.stereotype.Component;

/**
 * 流星之箭：
 * 伤害暴击后，会重创敌人，使目标受到持续流血伤害（3秒每秒50/75/100%）
 * 2级：同时附带致死效果，受到的治疗降低60%
 * 3级：流血伤害提升至70%
 * 4级：流血伤害提升至100%
 */
@Component
public class LiuXingZhiJian implements AttackPassive {
    @Override
    public PassiveType getType() {
        return PassiveType.LIU_XING_ZHI_JIAN;
    }

    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        boolean crit = context.isCrit(target);
        if (!crit) {
            return;
        }
        String[] buffIds = passiveState.getParam(String[].class);
        for (String buffId : buffIds) {
            BuffFactory.addBuff(buffId, owner, target, time, skillReport, null);
        }
    }
}
