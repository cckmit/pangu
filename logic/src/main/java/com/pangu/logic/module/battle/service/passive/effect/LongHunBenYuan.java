package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.service.action.Action;
import com.pangu.logic.module.battle.service.action.custom.TransformEndAction;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.OwnerDiePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.skill.SkillFactory;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.stream.Collectors;


/**
 * 死亡后复活并永久维持龙型，永久存在，只能触发一次
 */
@Component
public class LongHunBenYuan implements OwnerDiePassive {
    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attack, ITimedDamageReport timedDamageReport, int time, Context context) {
        //若以龙形死去，立即变回人形
        final PriorityQueue<Action> worldActions = owner.getBattle().getWorldActions();
        final List<Action> transformEndAct = worldActions.stream()
                .filter(act -> act instanceof TransformEndAction && ((TransformEndAction) act).getTarget() == owner)
                .collect(Collectors.toList());
        for (Action act : transformEndAct) {
            if (!owner.getBattle().removeWorldAction(act)) {
                continue;
            }
            ((TransformEndAction) act).setTime(time);
            act.execute();
        }

        final boolean success = owner.revive(time);
        if (!success) {
            return;
        }

        final long value = owner.getValue(UnitValue.HP) + context.getHpChange(owner);

        // 保留1点血
        context.addPassiveValue(owner, AlterType.HP, -value + 1);

//        timedDamageReport.add(time, owner.getId(), Hp.of(-value + 1));

        //变身为龙
        final String skillId = passiveState.getParam(String.class);
        final SkillState skillState = SkillFactory.initState(skillId);
        final Optional<EffectState> maxDelayEffect = skillState.getEffectStates().stream()
                .max(Comparator.comparingInt(EffectState::getDelay));
        int maxDelay = 0;
        if (maxDelayEffect.isPresent()) {
            maxDelay = maxDelayEffect.get().getDelay();
        }
        final int reviveDelay = skillState.getSingTime() + skillState.getFirstTimeDelay() + maxDelay;
        final int reviveTime = reviveDelay + time;
        owner.addState(UnitState.UNVISUAL, reviveTime);
        owner.addState(UnitState.WU_DI, reviveTime);
        owner.addState(UnitState.BA_TI, reviveTime);
        SkillFactory.updateNextExecuteSkill(time, owner, skillState);

        //更新cd和使用次数
        passiveState.addCD(time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.LONG_HUN_BEN_YUAN;
    }
}
