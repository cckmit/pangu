package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.param.DefaultAddValueParam;
import com.pangu.logic.module.battle.service.buff.utils.CalTypeHelper;
import com.pangu.logic.module.battle.service.buff.utils.CalValues;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.BeStateAddPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.BuffCastWhenControlledParam;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 在被添加控制状态前执行某些操作
 */
@Component
public class DoSthBeforeControl implements BeStateAddPassive {
    @Override
    public int beStateAddBefore(PassiveState passiveState, Unit owner, UnitState state, int time, int validTime, Context context, ITimedDamageReport damageReport) {
        if (!state.controlState()) {
            return validTime;
        }

        //  仅处理控制状态
        final BuffCastWhenControlledParam param = passiveState.getParam(BuffCastWhenControlledParam.class);
        if (!RandomUtils.isHit(param.getTriggerRate())) {
            return validTime;
        }
        passiveState.addCD(time);
        BuffFactory.addBuff(param.getBuff(), owner, owner, time, damageReport, null);

        final DefaultAddValueParam valModParam = param.getValModParam();
        if (valModParam != null) {
            final CalValues calValues = CalTypeHelper.calValues(valModParam.getCalType(), valModParam.getAlters(), owner, owner, valModParam.getFactor());
            for (Map.Entry<AlterType, Number> entry : calValues.getValues().entrySet()) {
                AlterType alterType = entry.getKey();
                Number number = entry.getValue();
                damageReport.add(time, owner.getId(), new UnitValues(alterType, number));
                context.addPassiveValue(owner, alterType, number);
            }
        }

        if (param.isDecontrol()) {
            // 生成战报供前端展示
            PassiveValue passiveValue = PassiveValue.of(passiveState.getId(), owner.getId());
            damageReport.add(time, owner.getId(), passiveValue);
            return -1;
        }

        return validTime;
    }

    @Override
    public PassiveType getType() {
        return PassiveType.DO_STH_WHEN_CONTROLLED;
    }
}
