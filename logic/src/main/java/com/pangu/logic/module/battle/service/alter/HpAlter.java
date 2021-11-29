package com.pangu.logic.module.battle.service.alter;


import com.pangu.logic.module.battle.model.AlterAfterValue;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.service.core.Unit;

/**
 * 血量修改器
 */
public class HpAlter extends IntegerTemplate {
    @Override
    public void execute(Unit unit, Number pre, AlterAfterValue alterAfter, int time) {
        long value = pre.longValue();
        if (unit.hasState(UnitState.WU_DI, time)    //当目标具备[无敌]状态
                && !unit.hasState(UnitState.WU_DI_INVALID, time)    //且不具备[无敌失效]状态
                && value < 0    //且当前调整为造成伤害时
        ) {
            //则免疫当次伤害
            alterAfter.put(UnitValue.HP, unit.getValue(UnitValue.HP));
            return;
        }
        if (value == 0) {
            return;
        }
        if (value < 0) {
            long shield = unit.getValue(UnitValue.SHIELD);
            if (shield > 0) {
                long shieldAfter = value + shield;
                // 护盾完全抵挡伤害
                if (shieldAfter > 0) {
                    unit.increaseValue(UnitValue.SHIELD, value);
                    alterAfter.put(UnitValue.SHIELD, shieldAfter);
                    return;
                } else {
                    unit.increaseValue(UnitValue.SHIELD, -shield);
                    alterAfter.put(UnitValue.SHIELD, 0);
                }
                // 继续往下扣除血量
                value = shieldAfter;
                // 完全抵挡，则不扣除血量
                if (value == 0) {
                    return;
                }
            }
        }
        long hpMax = unit.getValue(UnitValue.HP_MAX);
        long curHp = unit.getValue(UnitValue.HP);
        long afterHp = Math.max(0, curHp + value);
        if (afterHp > hpMax) {
            unit.setValue(UnitValue.HP, hpMax);
            alterAfter.put(UnitValue.HP, hpMax);
        } else {
            unit.setValue(UnitValue.HP, afterHp);
            alterAfter.put(UnitValue.HP, afterHp);
        }
        // 控制死亡
        // 死亡与否交由Context控制，以提高灵活度
//        if (afterHp == 0) {
//            unit.dead();
//        }
    }
}
