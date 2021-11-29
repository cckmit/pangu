package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class ConditionallyAttackChainParam {
    /** 伤害率*/
    private double rate;

    /** 触发条件表达式*/
    private String triggerCond;

    /** 连带目标*/
    private String target;
}
