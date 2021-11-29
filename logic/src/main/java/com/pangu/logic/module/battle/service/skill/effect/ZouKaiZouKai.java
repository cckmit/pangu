package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 走开走开(3级)
 * 有敌人靠近时，持续4秒在跟随的队友附近清除障碍，期间自己无法被攻击且每秒对附近敌人造成80%攻击力的伤
 * 害。
 * 2级:伤害提升至100%攻击力
 * 3级:伤害提升至120%攻击力
 * 4级：跟随的队友如果布阵在后排，则该技能还会将敌人击退
 */
@Component
public class ZouKaiZouKai implements SkillEffect {

    @Autowired
    private Repel repel;

    @Override
    public EffectType getType() {
        return EffectType.ZOU_KAI_ZOU_KAI;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        int loopTimes = context.getLoopTimes();
        if (loopTimes == 1 || loopTimes == skillState.getExecuteTimes()) {
            int executeTimes = skillState.getExecuteTimes();
            int executeInterval = skillState.getExecuteInterval();
            int stateTime = executeTimes * executeInterval;

            int validTime = stateTime + time;
            if (loopTimes == 1) {
                owner.addState(UnitState.BA_TI, validTime);
                owner.addState(UnitState.UNVISUAL, validTime);
            } else {
                owner.removeState(UnitState.BA_TI, validTime);
                owner.removeState(UnitState.UNVISUAL, validTime);
            }
        }
        Unit traceUnit = owner.getTraceUnit();
        if (traceUnit == null) {
            return;
        }
        if (traceUnit.getSequence() == 0 || traceUnit.getSequence() == 1) {
            return;
        }
        Integer distance = state.getParam(Integer.class);
        if (distance <= 0) {
            return;
        }
        repel.execute(state, owner, target, skillReport, time, skillState, context);
    }
}
