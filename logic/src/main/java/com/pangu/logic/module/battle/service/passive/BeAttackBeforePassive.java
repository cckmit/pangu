package com.pangu.logic.module.battle.service.passive;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.Unit;

/**
 * 被攻击之前触发
 * @author Kubby
 */
public interface BeAttackBeforePassive extends Passive {

    void beAttackBefore(PassiveState passiveState, EffectState effectState, Unit owner, Unit attacker, int time,
                        Context context, SkillReport skillReport);

    void beAttackEnd(PassiveState passiveState, EffectState effectState, Unit owner, Unit attacker, int time,
                     Context context, SkillReport skillReport);

}
