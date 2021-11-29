package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class BuffAddWhenRecoveredParam {
    /** 触发率*/
    private double triggerRate;

    /** 添加的buff*/
    private String buffId;
}
