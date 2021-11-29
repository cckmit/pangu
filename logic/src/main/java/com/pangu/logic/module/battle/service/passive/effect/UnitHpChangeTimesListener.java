package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.buff.param.DefaultAddValueParam;
import com.pangu.logic.module.battle.service.buff.utils.CalTypeHelper;
import com.pangu.logic.module.battle.service.buff.utils.CalValues;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitHpChangePassive;
import com.pangu.logic.module.battle.service.passive.param.UnitHpChangeTimesListenerParam;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 敌方合计受伤X次后，调整数值
 */
@Component
public class UnitHpChangeTimesListener implements UnitHpChangePassive {
    @Override
    public PassiveType getType() {
        return PassiveType.UNIT_HP_CHANGE_TIMES_LISTENER;
    }

    @Override
    public void hpChange(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> changeUnit) {
        final UnitHpChangeTimesListenerParam param = passiveState.getParam(UnitHpChangeTimesListenerParam.class);
        final boolean listenFriend = param.isListenFriend();
        final boolean listenEnemy = param.isListenEnemy();

        int count = 0;
        //仅统计进攻者对特定阵营单元所造成的伤害
        for (Unit unit : changeUnit) {
            if (context.getTotalHpChange(unit) >= 0) {
                continue;
            }
            if (owner.isFriend(unit) && listenFriend) {
                count++;
                continue;
            }
            if (!owner.isFriend(unit) && listenEnemy) {
                count++;
                continue;
            }
        }

        final int accCounts = count + passiveState.getAddition(Integer.class, 0);
        if (accCounts < param.getTriggerTimes()) {
            passiveState.setAddition(accCounts);
            return;
        }

        passiveState.addCD(time);

        //修改数值
        List<Unit> modTargets = new ArrayList<>(6);
        if (param.isModFriend()) {
            modTargets.addAll(owner.getFriend().getCurrent());
        }
        if (param.isListenEnemy()) {
            modTargets.addAll(owner.getEnemy().getCurrent());
        }

        final DefaultAddValueParam valModParam = param.getValModParam();
        for (Unit modTarget : modTargets) {
            final CalValues calValues = CalTypeHelper.calValues(valModParam.getCalType(), valModParam.getAlters(), owner, modTarget, valModParam.getFactor());
            for (Map.Entry<AlterType, Number> entry : calValues.getValues().entrySet()) {
                final AlterType alterType = entry.getKey();
                final Number value = entry.getValue();
                context.addPassiveValue(modTarget, alterType, value);
                damageReport.add(time, modTarget.getId(), new UnitValues(alterType, value));
            }
        }
    }
}
