package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.ShiftAttentionParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 寒冰精灵召唤出后会嘲讽攻击杰拉尔的敌人。此被动给杰拉尔装备，受到伤害时对攻击者施加嘲讽。
 */
@Component
public class ShiftAttention implements DamagePassive {
    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final ShiftAttentionParam param = passiveState.getParam(ShiftAttentionParam.class);
        final List<Unit> victims = TargetSelector.select(owner, param.getTarget(), time);
        if (CollectionUtils.isEmpty(victims)) {
            return;
        }
        PassiveUtils.addState(owner, attacker, UnitState.SNEER, param.getDur() + time, time, passiveState, context, skillReport);
        attacker.setTraceUnit(victims.get(0));
    }

    @Override
    public PassiveType getType() {
        return PassiveType.SHIFT_ATTENTION;
    }
}
