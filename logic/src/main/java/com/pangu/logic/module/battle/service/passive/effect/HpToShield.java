package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.RecoverPassive;
import org.springframework.stereotype.Component;

/**
 * 血量溢出值转换为护盾
 */
@Component
public class HpToShield implements RecoverPassive {
    @Override
    public PassiveType getType() {
        return PassiveType.HP_TO_SHIELD;
    }

    @Override
    public void recover(PassiveState passiveState, Unit owner, Unit from, long recover, int time, Context context, SkillState skillState, SkillReport skillReport) {
        // 是否存在恢复血量转换为护盾的属性
        double hpToShieldRate = passiveState.getParam(double.class);
        // 如果配置，则将恢复血量转换为护盾
        if (hpToShieldRate <= 0) {
            return;
        }
        long addHp = context.getHpChange(from);
        if (addHp <= 0) {
            return;
        }
        long hpMax = from.getValue(UnitValue.HP_MAX);
        long curHp = from.getValue(UnitValue.HP);
        long diff = curHp + addHp - hpMax;
        if (diff <= 0) {
            return;
        }
        long shieldValue = (long) (diff * hpToShieldRate);
        long curShield = from.getValue(UnitValue.SHIELD);
        // 只有增加的护盾值大于当前护盾值时，才会添加成功
        if (shieldValue <= curShield) {
            return;
        }
        context.addPassiveValue(owner, AlterType.SHIELD_SET, shieldValue);
        skillReport.add(time, owner.getId(), new UnitValues(AlterType.SHIELD_SET, shieldValue));
    }
}
