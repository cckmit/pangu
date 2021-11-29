package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitHpChangePassive;
import com.pangu.logic.module.battle.service.passive.param.JiBingNvHuangUnYieldParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 当生命值低于5%时，伊莎贝拉会释放寒冰将自己裹住，持续3秒，恢复25%的最大生命值。可被攻击，但免疫任何伤害与控制， 冰棺结束之后，立刻释放一次“冰封世界”
 */
@Component
public class JiBingNvHuangUnYield implements UnitHpChangePassive {
    @Autowired
    private OwnerEffectWhenSkillRelease modifier;

    @Override
    public void hpChange(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> changeUnit) {
        if (!changeUnit.contains(owner)) {
            return;
        }
        final JiBingNvHuangUnYieldParam param = passiveState.getParam(JiBingNvHuangUnYieldParam.class);
        final double hpPct = owner.getHpPct();
        if (hpPct > param.getTriggerHpPCT()) {
            return;
        }

        //  修改数值
        context.modVal(owner, owner, time, damageReport, param.getValModParam(), passiveState.getId(), null);

        //  添加状态
        final UnitState[] states = param.getStates();
        if (ArrayUtils.isEmpty(states) || param.getDur() <= 0) {
            return;
        }
        final PassiveValue passiveValue = PassiveValue.of(passiveState.getId(), owner.getId());
        for (UnitState state : states) {
            PassiveUtils.addState(owner, owner, state, time + param.getDur(), time, context, passiveValue, damageReport);
        }
    }

    @Override
    public PassiveType getType() {
        return PassiveType.JIBINGNVHUANG_UNYIELD;
    }
}
