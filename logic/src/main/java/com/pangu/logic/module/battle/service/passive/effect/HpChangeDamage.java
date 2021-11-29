package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.HpChangeDamageParam;
import org.springframework.stereotype.Component;

/**
 * 攻击方/被攻击方 血量越低/越高 伤害越高
 */
@Component
public class HpChangeDamage implements AttackPassive {

    @Override
    public PassiveType getType() {
        return PassiveType.HP_CHANGE_DAMAGE;
    }

    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (damage >= 0) {
            return;
        }
        final HpChangeDamageParam param = passiveState.getParam(HpChangeDamageParam.class);

        Unit ref;
        if (param.isTarget()) {
            ref = target;
        } else {
            ref = owner;
        }

        final double hpRate = ref.getValue(UnitValue.HP) / 1D / owner.getValue(UnitValue.HP_MAX);

        int times;
        if (param.isDecrease()) {
            //血量越高，伤害越高
            times = (int) (hpRate / param.getPreHpDecrease());
        } else {
            //血量越低伤害越高
            times = (int) ((1 - hpRate) / param.getPreHpDecrease());
        }

        if (times<=0) {
            return;
        }

        double increaseRate = param.getIncrease() * times;
        increaseRate = Math.min(param.getMax(), increaseRate);

        long increaseValue = (long) (damage * increaseRate);
        context.addPassiveValue(target, AlterType.HP, increaseValue);
        skillReport.add(time, target.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), Hp.of(increaseValue)));
        passiveState.addCD(time);
    }
}
