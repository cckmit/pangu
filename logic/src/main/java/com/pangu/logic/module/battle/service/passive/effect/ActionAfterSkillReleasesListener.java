package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Mark;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.param.ActionAfterSkillReleasesListenerParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import com.pangu.framework.utils.reflect.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * 累计释放数次技能后，更新被动持有者的下一个技能行为
 */
@Component
public class ActionAfterSkillReleasesListener implements SkillReleasePassive {
    @Override
    public PassiveType getType() {
        return PassiveType.ACTION_AFTER_SKILL_RELEASES_LISTENER;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time, Context context, SkillReport skillReport) {
        //纳入统计的技能类型
        final ActionAfterSkillReleasesListenerParam param = passiveState.getParam(ActionAfterSkillReleasesListenerParam.class);
        if (!ArrayUtils.contains(param.getSkillTypes(), skillState.getType())) {
            return;
        }

        //纳入统计的阵容
        final boolean attackerIsFriend = owner.getFriend() == attacker.getFriend();
        Integer skillReleaseCount = passiveState.getAddition(Integer.class, 0);
        if (param.isFriendInclude() && attackerIsFriend) {
            skillReleaseCount++;
        }
        if (param.isEnemyInclude() && !attackerIsFriend) {
            skillReleaseCount++;
        }

        //到达累计次数，累计清零，并释放下一次技能
        if (skillReleaseCount >= param.getTriggerCount()) {
            skillReleaseCount = 0;
            switch (param.getActionType()) {
                case SKILL_UPDATE: {
                    final String skillId = param.getSkillId();
                    if (StringUtils.isEmpty(skillId)) {
                        break;
                    }

                    SkillFactory.updateNextExecuteSkill(time, owner, skillId);
                    break;
                }
                case STATE_ADD: {
                    final StateAddParam stateAddParam = param.getStateAddParam();
                    if (stateAddParam == null) {
                        break;
                    }

                    final String stateTarget = param.getStateTarget();
                    List<Unit> targets;
                    if (StringUtils.isEmpty(stateTarget)) {
                        targets = owner.getFriend().getCurrent();
                    } else {
                        targets = TargetSelector.select(owner, stateTarget, time);
                    }
                    for (Unit target : targets) {
                        PassiveUtils.addState(owner, target, stateAddParam.getState(), time + stateAddParam.getTime(), time, passiveState, context, skillReport);
                    }
                    break;
                }
            }
            passiveState.addCD(time);
        }

        passiveState.setAddition(skillReleaseCount);
        final String ownerId = owner.getId();
        skillReport.add(time, owner.getId(), PassiveValue.single(passiveState.getId(), ownerId, new Mark(skillReleaseCount)));
    }
}
