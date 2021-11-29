package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class ZhiMingLianMaoParam {
    //回复量：最大生命值*rate
    private double rate;
    //需要重置的buffId
    private String buffId;
}
