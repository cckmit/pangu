package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class DmgUpOnTargetShiftParam {
    /** 增伤率*/
    private double dmgUpRate;
    /** 触发技能标签*/
    private String triggerTag;
}
