package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Immune;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.param.DefaultAddValueParam;
import com.pangu.logic.module.battle.service.buff.utils.CalTypeHelper;
import com.pangu.logic.module.battle.service.buff.utils.CalValues;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.BuffCastWhenControlledParam;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 受到控制时有概率为自身添加buff
 */
@Component
public class BuffCastWhenControlled implements DamagePassive {
    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        //已经被控制
        final boolean controlled = owner.underControl(time);

        //将要被控制
        final boolean toBeControlled = context.toBeControlled(owner);

        if (!(controlled || toBeControlled)) {
            return;
        }
        final BuffCastWhenControlledParam param = passiveState.getParam(BuffCastWhenControlledParam.class);
        if (!RandomUtils.isHit(param.getTriggerRate())) {
            return;
        }

        BuffFactory.addBuff(param.getBuff(), owner, owner, time, skillReport, null);

        final DefaultAddValueParam valModParam = param.getValModParam();
        if (valModParam != null) {
            final CalValues calValues = CalTypeHelper.calValues(valModParam.getCalType(), valModParam.getAlters(), owner, owner, valModParam.getFactor());
            for (Map.Entry<AlterType, Number> entry : calValues.getValues().entrySet()) {
                AlterType alterType = entry.getKey();
                Number number = entry.getValue();
                skillReport.add(time, owner.getId(), new UnitValues(alterType, number));
                context.addPassiveValue(owner, alterType, number);
            }
        }

        if (param.isDecontrol()) {
            // 清理即将到来的控制状态
            context.decontrol(owner);
            // 清理当下具有的控制状态
            owner.decontrol();
            // 生成战报供前端展示
            PassiveValue passiveValue = PassiveValue.of(passiveState.getId(), owner.getId());
            skillReport.add(time, owner.getId(), passiveValue);
            skillReport.add(time, owner.getId(), new Immune());
        }

        passiveState.addCD(time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.BUFF_CAST_WHEN_CONTROLLED;
    }
}
