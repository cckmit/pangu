package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.service.buff.utils.CalTypeHelper;
import com.pangu.logic.module.battle.service.buff.utils.CalValues;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Fighter;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitDiePassive;
import com.pangu.logic.module.battle.service.passive.param.ChangeSurvivorsValuesWhenHeroDieListenerParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 友方英雄每次死亡时，为幸存者们修改数值
 */
@Component
public class ChangeSurvivorsValuesWhenFriendHeroDieListener implements UnitDiePassive {
    @Override
    public PassiveType getType() {
        return PassiveType.CHANGE_SURVIVORS_VALUES_WHEN_FRIEND_HERO_DIE_LISTENER;
    }

    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> dieUnits) {
        final ChangeSurvivorsValuesWhenHeroDieListenerParam param = passiveState.getParam(ChangeSurvivorsValuesWhenHeroDieListenerParam.class);
        boolean added = false;

        final Fighter fighter ;
        if (param.isFriend()) {
            fighter = owner.getFriend();
        } else {
            fighter = owner.getEnemy();
        }

        for (Unit dieUnit : dieUnits) {
            if (dieUnit.getFriend() != fighter) {
                continue;
            }
            if (dieUnit.isSummon()) {
                continue;
            }
            final List<Unit> friends = fighter.getCurrent();
            for (Unit survivor : friends) {
                final CalValues calValues = CalTypeHelper.calValues(param.getCalType(), param.getAlters(), owner, survivor, param.getFactor());
                for (Map.Entry<AlterType, Number> entry : calValues.getValues().entrySet()) {
                    AlterType alterType = entry.getKey();
                    Number number = entry.getValue();
                    context.addPassiveValue(survivor, alterType, number);
                }
            }
            added = true;
        }
        if (added) {
            passiveState.addCD(time);
        }
    }
}
