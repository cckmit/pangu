package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.param.UpdateSkillByTagSkillReleaseParam;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.stereotype.Component;

/**
 * 释放普攻时有一定概率立即更新下一次行动
 */
@Component
public class UpdateSkillByTagSkillRelease implements SkillReleasePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.UPDATE_SKILL_BY_TAG_SKILL_RELEASE;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time, Context context, SkillReport skillReport) {
        // 释放者鉴定
        if (owner != attacker) {
            return;
        }

        final UpdateSkillByTagSkillReleaseParam param = passiveState.getParam(UpdateSkillByTagSkillReleaseParam.class);

        //技能标签鉴定
        if (!skillState.getTag().equals(param.getTriggerSkillTag())) {
            return;
        }

        //触发概率鉴定
        if (!RandomUtils.isHit(param.getRate())) {
            return;
        }

        //通过鉴定，更新下次行动
        SkillFactory.updateNextExecuteSkill(time, owner, param.getSkillId());
    }
}
