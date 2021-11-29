package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.buff.utils.CalTypeHelper;
import com.pangu.logic.module.battle.service.buff.utils.CalValues;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.RecoverPassive;
import com.pangu.logic.module.battle.service.passive.param.LowHpAddValuesParam;
import org.springframework.stereotype.Component;

import java.util.Map;

//血量低于多少时给自己添加一些属性
@Component
public class LowHpAddValues implements DamagePassive, RecoverPassive {

    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        CalValues calValues = passiveState.getAddition(CalValues.class);
        if (calValues != null) {
            return;
        }
        final LowHpAddValuesParam param = passiveState.getParam(LowHpAddValuesParam.class);
        final long change = context.getHpChange(owner);
        final double hpRate = owner.getValue(UnitValue.HP) + change / 1D / owner.getValue(UnitValue.HP_MAX);
        if (param.isGreater() && hpRate < param.getHpRate() || !param.isGreater() && hpRate > param.getHpRate()) {
            return;
        }
        calValues = CalTypeHelper.calValues(param.getCalType(), param.getAlters(), owner, attacker, param.getFactor());
        for (Map.Entry<AlterType, Number> entry : calValues.getValues().entrySet()) {
            context.addPassiveValue(owner, entry.getKey(), entry.getValue());
            skillReport.add(time, owner.getId(), new UnitValues(entry.getKey(), entry.getValue()));
        }
        passiveState.setAddition(calValues);
    }

    @Override
    public void recover(PassiveState passiveState, Unit owner, Unit from, long recover, int time, Context context, SkillState skillState, SkillReport skillReport) {
        CalValues calValues = passiveState.getAddition(CalValues.class);
        if (calValues == null) {
            return;
        }
        final LowHpAddValuesParam param = passiveState.getParam(LowHpAddValuesParam.class);
        final long change = context.getHpChange(owner);
        final double hpRate = owner.getValue(UnitValue.HP) + change / 1D / owner.getValue(UnitValue.HP_MAX);
        if (param.isGreater() && hpRate < param.getHpRate() || !param.isGreater() && hpRate > param.getHpRate()) {
            for (Map.Entry<AlterType, Number> entry : calValues.getValues().entrySet()) {
                final AlterType key = entry.getKey();
                final Number reverse = key.getAlter().getReverse(entry.getValue());
                context.addPassiveValue(owner, key, reverse);
                skillReport.add(time, owner.getId(), new UnitValues(key, reverse));
            }
            passiveState.setAddition(null);
        }
    }

    @Override
    public PassiveType getType() {
        return PassiveType.LOW_HP_ADD_VALUES;
    }
}
