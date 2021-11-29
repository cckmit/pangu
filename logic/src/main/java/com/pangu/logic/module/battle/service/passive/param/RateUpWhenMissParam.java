package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class RateUpWhenMissParam {
    /** 命中提升率*/
    private double hitUpRate = 10;

    /** 增伤率*/
    private double harmUpRate = 0.3;

    /** 触发CD*/
    private int cd = 10000;
}
