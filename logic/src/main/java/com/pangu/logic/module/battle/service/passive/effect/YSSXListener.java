package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Mark;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.action.Action;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.param.YSSXParam;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import org.springframework.stereotype.Component;

/**
 * 源石神像监听器。当友方释放X次大招后，触发一个技能
 */
@Component
public class YSSXListener implements SkillReleasePassive {
    @Override
    public PassiveType getType() {
        return PassiveType.YSSX_LISTENER;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time, Context context, SkillReport skillReport) {
        
        if (SkillType.SPACE != skillState.getType()) {
            return;
        }
        if (!attacker.isFriend(owner)) {
            return;
        }

        
        final String ownerId = owner.getId();
        if (attacker == owner) {
            passiveState.setAddition(0);
            skillReport.add(time, owner.getId(), PassiveValue.single(passiveState.getId(), ownerId, new Mark(0)));
            return;
        }

        final YSSXParam param = passiveState.getParam(YSSXParam.class);
        final int triggerSpaceTimes = param.getTriggerSpaceTimes();
        Integer spaceReleaseCount = passiveState.getAddition(Integer.class);
        if (spaceReleaseCount == null) {
            spaceReleaseCount = 2;  //此处写死用于线上热更
        }
        if (spaceReleaseCount < triggerSpaceTimes) {
            spaceReleaseCount++;
            passiveState.setAddition(spaceReleaseCount);
            skillReport.add(time, owner.getId(), PassiveValue.single(passiveState.getId(), ownerId, new Mark(spaceReleaseCount)));

            if (spaceReleaseCount == triggerSpaceTimes) {
                final SkillState spaceState = SkillFactory.initState(param.getSpaceId());
                if (spaceState.getType() == SkillType.SPACE) {
                    final int spaceCD = owner.getBattle().getSpaceCD();
                    final int actTime = Math.max(spaceCD, time);
                    final Action action = new Action() {
                        @Override
                        public int getTime() {
                            return actTime;
                        }

                        @Override
                        public void execute() {
                            SkillFactory.updateSpace(getTime(), owner, spaceState);
                        }
                    };
                    owner.addTimedAction(action);
                }
            }
        }
    }
}
