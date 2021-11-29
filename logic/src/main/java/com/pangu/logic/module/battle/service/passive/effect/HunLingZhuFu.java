package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitHpChangePassive;
import com.pangu.logic.module.battle.service.passive.param.HunLingZhuFuParam;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 魂灵祝福：
 * 战斗中，自己每秒恢复0.6%最大生命值。场上召唤物以外的角色(含敌方) 每累计 恢复该角色10%最大生命值
 * 的血量后，恢复自身3.5%最大生命值的血量。
 * 2级:战斗中，自己每秒恢复0.8%最大生命值
 * 3级:战斗中，自己每秒恢复1%最大生命值。
 * 4级:战斗中，自己每秒恢复1.2%最大生命值。
 */
@Component
public class HunLingZhuFu implements UnitHpChangePassive {
    @Override
    public void hpChange(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> changeUnit) {
        HunLingZhuFuParam param = passiveState.getParam(HunLingZhuFuParam.class);

        HunLingZhuFuValue addition = passiveState.getAddition(HunLingZhuFuValue.class, new HunLingZhuFuValue());
        Map<Unit, Long> preHps = addition.preHp;
        Map<Unit, Long> addHps = addition.addHp;

        int unitRecoverRate = param.getEnemyRecoverPercent();
        double recoverRate = param.getRecoverRate();
        long ownerHp = owner.getValue(UnitValue.HP_MAX);
        PassiveValue passiveValue = null;
        for (Unit unit : changeUnit) {
            if (unit.isSummon()) {
                continue;
            }
            long curHp = unit.getValue(UnitValue.HP);
            Long preHp = preHps.get(unit);
            long hpMax = unit.getValue(UnitValue.HP_MAX);
            if (preHp == null) {
                preHp = hpMax;
            }
            long change = curHp - preHp;
            if (change > 0) {
                Long cur = addHps.merge(unit, change, Long::sum);
                long curPercent = cur * 100 / hpMax;
                long recoverPercentAmount = curPercent / unitRecoverRate;
                if (recoverPercentAmount == 0) {
                    continue;
                }
                long addHp = (long) (recoverPercentAmount * recoverRate * ownerHp);
                if (passiveValue == null) {
                    passiveValue = PassiveValue.of(passiveState.getId(), owner.getId());
                }
                passiveValue.add(Hp.fromRecover(addHp));
                addHps.put(unit, (long) ((curPercent % unitRecoverRate) * 0.01 * hpMax));
                context.addPassiveValue(owner, AlterType.HP, addHp);

            }
            preHps.put(unit, curHp);
        }
        if (passiveValue != null) {
            damageReport.add(time, owner.getId(), passiveValue);
        }
    }

    @Override
    public PassiveType getType() {
        return PassiveType.HUN_LING_ZHU_FU;
    }

    private static class HunLingZhuFuValue {
        Map<Unit, Long> preHp = new HashMap<>(12);
        Map<Unit, Long> addHp = new HashMap<>(12);
    }
}
