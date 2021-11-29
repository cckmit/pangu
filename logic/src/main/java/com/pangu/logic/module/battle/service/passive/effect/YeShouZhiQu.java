package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.YeShouZhiQuParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import org.springframework.stereotype.Component;

/**
 * 野兽之躯
 * 受到不超过自己最大生命值5%的小额伤害时,伤害值下降30%
 * 2级:降低伤害的条件变为不超过自己最大生命值的7%
 * 3级:降低伤害的条件变为不超过自己最大生命值的9%
 * 4级:伤害值下降45%
 */
@Component
public class YeShouZhiQu implements DamagePassive {
    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final YeShouZhiQuParam param = passiveState.getParam(YeShouZhiQuParam.class);
        final long hpChange = context.getHpChange(owner);
        if (hpChange >= 0) {
            return;
        }
        final boolean triggered = hpChange > -param.getTriggerHpPct() * owner.getValue(UnitValue.HP_MAX);
        if (!triggered) {
            return;
        }
        final long hpRecover = -(long)(hpChange * param.getDmgCutRate());
        PassiveUtils.hpUpdate(context, skillReport, owner, hpRecover, time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.YE_SHOU_ZHI_QU;
    }
}
