package com.pangu.logic.module.battle.service.action;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.core.*;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 单个效果执行
 */
@Getter
public class EffectAction implements Action {

    private final int time;

    private final Unit owner;

    private final SkillState skillState;

    private final SkillReport skillReport;

    private final EffectState effectState;

    private List<Unit> targets;

    @Setter
    private SelectSetting dynamicSelectSetting;
    @Setter
    private SkillEffect effect;
    @Setter
    private SkillEffectAction rootSkillEffectAction;
    @Setter
    private Context context;

    public EffectAction(int time, Unit owner, SkillState skillState, SkillReport skillReport, EffectState effectState, List<Unit> targets) {
        this.time = time;
        this.owner = owner;
        this.skillState = skillState;
        this.skillReport = skillReport;
        this.effectState = effectState;
        this.targets = targets;
        this.context = new Context(owner);
    }

    @Override
    public void execute() {
        init();

        // 对每个施放目标做独立运算
        for (Unit target : targets) {
            context.execAttackBeforePassiveAndEffect(time,effect,target,skillState,effectState,skillReport);
        }

        context.execute(time, skillState, skillReport);
    }

    private void init() {
        //动态传入技能效果
        if (effect == null) effect = SkillFactory.getSkillEffect(effectState.getId());
        //对于配置了目标选择延时的EffectState，重新选择目标
        if (effectState.isReselectTargets()) targets = TargetSelector.select(owner, effectState.getTarget(), time);
        //动态选择目标
        if (dynamicSelectSetting != null) targets = TargetSelector.select(owner, time, dynamicSelectSetting);
        //初始化Context
        if (context == null) context = new Context(owner);
        if (rootSkillEffectAction != null) {
            context.setRootSkillEffectAction(rootSkillEffectAction);
            context.setLoopTimes(rootSkillEffectAction.getExecuteTimes());
        }
    }
}
