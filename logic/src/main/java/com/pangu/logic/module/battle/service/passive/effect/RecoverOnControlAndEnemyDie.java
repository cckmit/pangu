package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.values.Mark;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.StateAddPassive;
import com.pangu.logic.module.battle.service.passive.UnitDiePassive;
import com.pangu.logic.module.battle.service.passive.param.RecoverOnControlOrEnemyDieParam;
import com.pangu.logic.module.battle.service.skill.effect.HpRecover;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 塔巴斯每次在对敌方英雄造成晕眩或位移时，或附近有敌人阵亡时，会积攒他的怒吼层数。在5层时，他会治疗他自己，获得300%攻击力的治疗效果
 */
@Component
public class RecoverOnControlAndEnemyDie implements StateAddPassive, UnitDiePassive {
    @Autowired
    private HpRecover recover;

    @Override
    public PassiveType getType() {
        return PassiveType.RECOVER_ON_CONTROL_OR_ENEMY_DIE;
    }

    @Override
    public void stateAddAfter(PassiveState passiveState, Unit owner, Unit target, UnitState state, int time, int validTime, Context context, ITimedDamageReport damageReport) {
        final RecoverOnControlOrEnemyDieParam param = passiveState.getParam(RecoverOnControlOrEnemyDieParam.class);
        int count = passiveState.getAddition(Integer.class, 0);
        if (state.controlState()) {
            count++;
        }
        doRecoverAndUpdateCount(passiveState, owner, time, context, damageReport, param, count);
    }

    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> dieUnits) {
        final RecoverOnControlOrEnemyDieParam param = passiveState.getParam(RecoverOnControlOrEnemyDieParam.class);
        int count = passiveState.getAddition(Integer.class, 0);
        for (Unit dieUnit : dieUnits) {
            if (owner.calcDistance(dieUnit) < param.getR()) {
                count++;
            }
        }
        doRecoverAndUpdateCount(passiveState, owner, time, context, damageReport, param, count);
    }

    private void doRecoverAndUpdateCount(PassiveState passiveState, Unit target, int time, Context context, ITimedDamageReport damageReport, RecoverOnControlOrEnemyDieParam param, int count) {
        final int triggerTimes = param.getTriggerTimes();
        if (count >= triggerTimes) {
            count -= triggerTimes;
            final HpRecover.RecoverResult res = recover.calcRecoverRes(target, target, param.getRecParam());
            context.passiveRecover(target, target, res.getRecover(), time, passiveState, damageReport);
        }
        passiveState.setAddition(count);
        final String ownerId = target.getId();
        damageReport.add(time, ownerId, PassiveValue.single(passiveState.getId(), ownerId, new Mark(count)));
    }
}
