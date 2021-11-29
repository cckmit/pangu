package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackEndPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import org.springframework.stereotype.Component;

/**
 * 攻击暴击后给自己添加BUFF
 */
@Component
public class CritAddBuffs implements AttackEndPassive {
    @Override
    public void attackEnd(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final boolean crit = context.isCrit(target);
        if (!crit) {
            return;
        }
        final String[] param = passiveState.getParam(String[].class);
        for (String buff : param) {
            BuffFactory.addBuff(buff, owner, owner, time, skillReport, null);
        }
        passiveState.addCD(time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.CRIT_ADD_BUFFS;
    }
}
