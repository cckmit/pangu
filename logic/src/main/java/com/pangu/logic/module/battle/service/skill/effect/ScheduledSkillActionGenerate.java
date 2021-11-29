package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.action.custom.ScheduledSkillUpdateAction;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import com.pangu.logic.module.battle.service.skill.param.ScheduledSkillActionGenerateParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 按照预设时程更新自身行为
 */
@Component
public class ScheduledSkillActionGenerate implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.SCHEDULED_SKILL_ACTION_GENERATE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final ScheduledSkillActionGenerateParam param = state.getParam(ScheduledSkillActionGenerateParam.class);
        final Map<String, List<Integer>> skillIdActionTimeMap = param.getSkillIdActionTimeMap();

        for (Map.Entry<String, List<Integer>> entry : skillIdActionTimeMap.entrySet()) {
            final String skillId = entry.getKey();
            final List<Integer> exeTimeList = entry.getValue();

            //饿汉式初始化技能状态，以提升性能
            final SkillState scheduledSkillState = SkillFactory.initState(skillId);
            for (Integer exeTime : exeTimeList) {
                final ScheduledSkillUpdateAction scheduledSkillUpdateAction = new ScheduledSkillUpdateAction(exeTime, scheduledSkillState, owner);
                if (skillState.getSetting().isIgnoreDie()) {
                    owner.getBattle().addWorldAction(scheduledSkillUpdateAction);
                } else {
                    owner.addTimedAction(scheduledSkillUpdateAction);
                }
            }
        }
    }
}
