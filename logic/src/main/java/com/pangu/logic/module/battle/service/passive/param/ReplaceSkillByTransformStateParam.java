package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class ReplaceSkillByTransformStateParam {
    /** 触发被动的变身状态*/
    private int triggerState;

    /** 触发被动的技能标签*/
    private String triggerId;

    /** 替换的技能id*/
    private String replacingId;
}
