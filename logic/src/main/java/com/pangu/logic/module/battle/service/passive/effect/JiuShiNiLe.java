package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Fighter;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitDiePassive;
import com.pangu.logic.module.battle.service.passive.UnitHpChangePassive;
import com.pangu.logic.module.battle.service.passive.param.JiuShiNiLeParam;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 就是你了(3级)
 * 当场上出现生命值低于30%的敌人时持续追踪该单位直至其死亡， 期间优先攻击该敌人，普通攻击提升35%的攻击力。
 * 2级:追踪期间，获得10%的吸血效果
 * 3级:攻击力提升效果提升至50%
 * 4级:触发效果提升至生命值低于40%
 */
@Component
public class JiuShiNiLe implements UnitHpChangePassive, UnitDiePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.JIU_SHI_NI_LE;
    }

    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> dieUnits) {
        if (!owner.hasState(UnitState.ZHUI_JI, time)) {
            return;
        }
        Unit traceUnit = owner.getTraceUnit();
        if (traceUnit == null) {
            return;
        }
        if (!dieUnits.contains(traceUnit)) {
            return;
        }
        owner.setTraceUnit(null);

        BuffState buffState = passiveState.getAddition(BuffState.class);
        if (buffState == null) {
            return;
        }
        BuffFactory.removeBuffState(buffState, owner, time);
    }

    @Override
    public void hpChange(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> changeUnit) {
        if (changeUnit == null || changeUnit.isEmpty()) {
            return;
        }
        // 追击目标未死亡，直接忽视
        if (owner.hasState(UnitState.ZHUI_JI, time)) {
            Unit traceUnit = owner.getTraceUnit();
            if (traceUnit != null && !traceUnit.isDead()) {
                return;
            }
        }
        JiuShiNiLeParam param = passiveState.getParam(JiuShiNiLeParam.class);
        int hpPercent = param.getRate();
        Unit validUnit = null;
        Fighter friend = owner.getFriend();
        for (Unit unit : changeUnit) {
            if (unit.isDead() || unit.getFriend() == friend) {
                continue;
            }
            long hp = unit.getValue(UnitValue.HP);
            long hpMax = unit.getValue(UnitValue.HP_MAX);
            if ((hp * 100 / hpMax) > hpPercent) {
                continue;
            }
            validUnit = unit;
            break;
        }
        if (validUnit == null) {
            return;
        }
        // 设置追击目标
        owner.setTraceUnit(validUnit);
        owner.addState(UnitState.ZHUI_JI);

        // 获取要施放的BUFF信息
        String buffId = param.getBuff();
        BuffState buffState = BuffFactory.addBuff(buffId, owner, owner, time, damageReport, null);
        if (buffState != null) {
            passiveState.setAddition(buffState);
        }
    }
}
