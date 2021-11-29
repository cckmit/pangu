package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class ConditionallyDmgUpParam {
    /** 触发条件表达式*/
    private String triggerExp;

    /** 增伤率*/
    private double dmgUpRate;

    /** 增伤表达式*/
    private String dmgExp;
}
