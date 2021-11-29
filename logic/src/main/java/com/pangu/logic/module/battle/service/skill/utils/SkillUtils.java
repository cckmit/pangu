package com.pangu.logic.module.battle.service.skill.utils;

import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.values.Immune;
import com.pangu.logic.module.battle.model.report.values.StateAdd;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;

public class SkillUtils {
    /**
     *
     * @param caster
     * @param unit         被添加异常的目标
     * @param state        异常状态
     * @param time         当前时刻
     * @param validTime    过期时刻
     * @param damageReport 战报
     *                     <p>
     *                     该方法主要用于在【主动】效果中实现添加异常状态，某些特殊情况下（如被动效果实际为主动效果的一部分）可以适用于被动效果中。
     *                     在【被动】效果中添加状态时，请尽量使用：
     * @param context      上下文
     * @see PassiveUtils#addState(Unit, Unit, UnitState, int, int, com.pangu.logic.module.battle.service.passive.PassiveState, Context, ITimedDamageReport)
     */
    public static boolean addState(Unit caster, Unit unit, UnitState state, int time, int validTime, ITimedDamageReport damageReport, Context context) {
        final int trueValidTime = context.addState(caster, unit, state, time, validTime, damageReport);
        if (damageReport != null) {
            if (trueValidTime <= time) {
                damageReport.add(time, unit.getId(), new Immune());
                return false;
            }
            damageReport.add(time, unit.getId(), new StateAdd(state, trueValidTime));
        }
        return true;
    }
}
