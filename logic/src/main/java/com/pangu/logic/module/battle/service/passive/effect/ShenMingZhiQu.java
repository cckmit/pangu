package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Immune;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.DispelType;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 神明之躯(3级)
 * 自身受到负面状态影响时，无视影响解除自身所有负面状态。该技能有10秒冷却。
 * 2级:成功解除后，获得一个抵御200%攻击力伤害的护盾。
 * 3级:护盾抵御值上升至250%攻击力
 * 4级:护盾抵御值上升至300%攻击力
 */
@Component
public class ShenMingZhiQu implements DamagePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.SHEN_MING_ZHI_QU;
    }

    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        boolean hasHarmState = context.hasHarmState(owner);
        List<BuffState> harmBuff = owner.getBuffByDispel(DispelType.HARMFUL);
        final boolean harmBuffEmpty = CollectionUtils.isEmpty(harmBuff);
        if (!hasHarmState && harmBuffEmpty) {
            return;
        }

        passiveState.addCD(time);

        // 清理负面状态
        context.clearHarmState(owner);
        if (!harmBuffEmpty) {
            for (BuffState buffState : harmBuff.toArray(new BuffState[0])) {
                BuffFactory.removeBuffState(buffState, owner, time);
            }
        }

        PassiveValue passiveValue = PassiveValue.of(passiveState.getId(), owner.getId());
        skillReport.add(time, owner.getId(), passiveValue);
        skillReport.add(time, owner.getId(), new Immune());

        Double param = passiveState.getParam(Double.class);
        if (param == 0) {
            return;
        }
        long shield = (long) (param * Math.max(owner.getValue(UnitValue.ATTACK_M), owner.getValue(UnitValue.ATTACK_P)));
        if (shield == 0) {
            return;
        }
        passiveValue.add(new UnitValues(AlterType.SHIELD_SET, shield));
        context.addPassiveValue(owner, AlterType.SHIELD_SET, shield);
    }
}
