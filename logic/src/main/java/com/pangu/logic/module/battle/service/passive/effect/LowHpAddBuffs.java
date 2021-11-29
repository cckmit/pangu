package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.LowHpAddBuffsParam;
import org.springframework.stereotype.Component;

/**
 * 血量低时给自己添加BUFF
 */
@Component
public class LowHpAddBuffs implements DamagePassive {
    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final long value = owner.getValue(UnitValue.HP) + context.getHpChange(owner);
        final LowHpAddBuffsParam param = passiveState.getParam(LowHpAddBuffsParam.class);
        final double hpRate = value / 1D / owner.getValue(UnitValue.HP_MAX);
        if (hpRate <= param.getHpRate()) {
            for (String buff : param.getBuffs()) {
                BuffFactory.addBuff(buff, owner, owner, time, skillReport, null);
            }
            passiveState.addCD(time);
        }
    }

    @Override
    public PassiveType getType() {
        return PassiveType.LOW_HP_ADD_BUFF;
    }
}
