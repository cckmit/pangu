package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.action.Action;
import com.pangu.logic.module.battle.service.action.SkillEffectAction;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitDiePassive;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * 在火力支援技能中击杀目标或参与击杀目标都将使火力支援的攻击时长延长2秒
 */
@Component
public class HuoLiZhiYuanExtend implements UnitDiePassive, AttackPassive {
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (skillState.getEffectStates().get(0).getType() != EffectType.HUO_LI_ZHI_YUAN) {
            return;
        }

        final Set<Unit> attacked = passiveState.getAddition(Set.class, new HashSet());
        attacked.add(target);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.HUO_LI_ZHI_YUAN_EXTEND;
    }

    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> dieUnits) {
        final Set<Unit> attacked = passiveState.getAddition(Set.class, new HashSet());

        int count = 0;
        for (Unit dieUnit : dieUnits) {
            if (attacked.contains(dieUnit)) {
                count++;
                attacked.remove(dieUnit);
            }
        }

        if (count == 0) {
            return;
        }

        final int loopPlusPerKill = passiveState.getParam(Integer.class);
        final int totalAddLoops = loopPlusPerKill * count;

        //  本轮技能就是火力支援
        final SkillEffectAction rootSkillEffectAction = context.getRootSkillEffectAction();
        if (rootSkillEffectAction.getSkillState().getEffectStates().get(0).getType() == EffectType.HUO_LI_ZHI_YUAN) {
            rootSkillEffectAction.addTotalExecTimes(totalAddLoops);
            return;
        }

        //  本轮技能不是火力支援
        for (Action timeAction : owner.getTimeActions()) {
            if (!(timeAction instanceof SkillEffectAction)) {
                continue;
            }
            final SkillEffectAction skillAction = (SkillEffectAction) timeAction;
            if (skillAction.getSkillState().getEffectStates().get(0).getType() != EffectType.HUO_LI_ZHI_YUAN) {
                continue;
            }
            skillAction.addTotalExecTimes(totalAddLoops);
        }
    }
}
