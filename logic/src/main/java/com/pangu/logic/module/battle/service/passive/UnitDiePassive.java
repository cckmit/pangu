package com.pangu.logic.module.battle.service.passive;

import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.List;
import java.util.Set;

/**
 * 单元死亡被动
 */
public interface UnitDiePassive extends Passive {

    /**
     * @param passiveState
     * @param owner
     * @param attacker
     * @param time
     * @param context
     * @param damageReport
     * @param dieUnits     死亡的单位集合
     */
    void die(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> dieUnits);
}
