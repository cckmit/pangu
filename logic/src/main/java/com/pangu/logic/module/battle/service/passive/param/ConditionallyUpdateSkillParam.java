package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class ConditionallyUpdateSkillParam {
    //  触发条件
    private String triggerCond;

    //  技能ID
    private String skillPrefix;

    //  是否为大招
    private boolean space;
}
