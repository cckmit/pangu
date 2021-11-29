package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.buff.utils.CalTypeHelper;
import com.pangu.logic.module.battle.service.buff.utils.CalValues;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.StealValuesParam;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 偷取或削弱受到伤害的单元属性
 */
@Component
public class StealValues implements AttackPassive {

    @Override
    public PassiveType getType() {
        return PassiveType.STEAL_VALUES;
    }

    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        List addition = passiveState.getAddition(List.class);
        if (addition != null && addition.contains(target)) {
            return;
        }
        final StealValuesParam param = passiveState.getParam(StealValuesParam.class);
        if (!RandomUtils.isHit(param.getHitRate())) {
            return;
        }
        final CalValues calValues = CalTypeHelper.calValues(param.getCalType(), param.getAlters(), owner, target, param.getFactor());
        final Map<AlterType, Number> values = calValues.getValues();
        //判断是否需要偷取属性
        if (param.getPassiveId() == null || owner.getPassiveStates(param.getPassiveId()) != null) {
            for (Map.Entry<AlterType, Number> entry : values.entrySet()) {
                AlterType alterType = entry.getKey();
                Number number = entry.getValue();
                final Number reverse = alterType.getAlter().getReverse(number);
                context.addPassiveValue(owner, alterType, number);
                context.addPassiveValue(target, alterType, reverse);
                skillReport.add(time, owner.getId(), new UnitValues(alterType, number));
                skillReport.add(time, target.getId(), new UnitValues(alterType, reverse));
            }
        } else {
            for (Map.Entry<AlterType, Number> entry : values.entrySet()) {
                AlterType alterType = entry.getKey();
                Number number = entry.getValue();
                final Number reverse = alterType.getAlter().getReverse(number);
                context.addPassiveValue(target, alterType, reverse);
                skillReport.add(time, target.getId(), new UnitValues(alterType, reverse));
            }
        }
        if (addition == null) {
            addition = new ArrayList<Unit>(6);
            passiveState.setAddition(addition);
        }
        addition.add(target);
        passiveState.addCD(time);
    }
}
