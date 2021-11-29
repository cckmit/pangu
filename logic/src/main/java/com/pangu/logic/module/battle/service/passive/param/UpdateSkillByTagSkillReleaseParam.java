package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class UpdateSkillByTagSkillReleaseParam {
    //触发概率
    private double rate;

    //下一个技能
    private String skillId;

    //触发标签
    private String triggerSkillTag;
}
