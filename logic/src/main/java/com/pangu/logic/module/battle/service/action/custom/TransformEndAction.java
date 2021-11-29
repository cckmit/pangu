package com.pangu.logic.module.battle.service.action.custom;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.RemovePassive;
import com.pangu.logic.module.battle.model.report.values.TransformValue;
import com.pangu.logic.module.battle.service.action.Action;
import com.pangu.logic.module.battle.service.action.SkillAction;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.skill.effect.TransformEffect;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;


/**
 * 解除变身状态，将被动、Buff重置为变身前的状态
 */
@Data
@Builder
public class TransformEndAction implements Action {
    // 变身结束时间
    private int time;

    // 变身技能的状态
    private EffectState effectState;

    // 技能战报
    private SkillReport skillReport;

    //被施加了变身的对象
    private Unit target;

    @Override
    public int getTime() {
        return time;
    }

    @Override
    public void execute() {
        final TransformEffect.Addition addition = effectState.getAddition(TransformEffect.Addition.class);

        final Set<PassiveState> transformPassives = addition.getTransformPassives();
        final Set<PassiveState> originPassives = addition.getOriginPassives();
        final List<BuffState> transformBuffs = addition.getTransformBuffs();
        final List<String> originBuffs = addition.getOriginBuffs();
        //移除变身时被动、BUFF
        for (PassiveState state : transformPassives) {
            target.removePassive(state);
            skillReport.add(time, target.getId(), new RemovePassive(state.getId()));
        }
        for (BuffState transformBuff : transformBuffs) {
            BuffFactory.removeBuffState(transformBuff, target, time);
        }

        //添加常态时被动、BUFF
        for (PassiveState passiveState : originPassives) {
            target.addPassive(passiveState, target);
//            skillReport.add(time, target.getId(), new AddPassive(passiveState.getId()));
        }
        for (String originBuff : originBuffs) {
            BuffFactory.addBuff(originBuff, target, target, time, skillReport, null);
        }
        //清空缓存
        transformPassives.clear();
        originPassives.clear();
        transformBuffs.clear();
        originBuffs.clear();

        //中断当前技能咏唱，防止变身结束后释放变身状态下的技能
        final Action curACT = target.getAction();
        if (curACT instanceof SkillAction) {
            ((SkillAction) curACT).broken(time);
        } else {
            target.reset(time);
        }
        //打印战报通知前端变身结束要变回的模型
        target.setTransformState(0);
        skillReport.add(time, target.getId(), new TransformValue(0));
    }
}
