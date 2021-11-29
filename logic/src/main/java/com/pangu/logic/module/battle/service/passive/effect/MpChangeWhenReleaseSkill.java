package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.param.MpCutWhenReleaseSkillParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 释放大招时调整目标怒气
 */
@Component
public class MpChangeWhenReleaseSkill implements SkillReleasePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.MP_CHANGE_WHEN_RELEASE_SKILL;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time, Context context, SkillReport skillReport) {
        final MpCutWhenReleaseSkillParam param = passiveState.getParam(MpCutWhenReleaseSkillParam.class);

        //非释放指定类型技能不生效
        if (!Arrays.asList(param.getSkillType()).contains(skillState.getType())) {
            return;
        }

        //触发概率鉴定
        if (!RandomUtils.isHit(param.getProb())) {
            return;
        }

        //筛选出需要修改怒气的目标
        final List<Unit> targetsToBeCutMp = TargetSelector.select(owner, param.getTargetId(), time);
        //为目标修改怒气
        for (Unit target : targetsToBeCutMp) {
            PassiveUtils.mpUpdate(context, skillReport, owner, target, param.getMpChange(), time, passiveState);
        }
    }
}
