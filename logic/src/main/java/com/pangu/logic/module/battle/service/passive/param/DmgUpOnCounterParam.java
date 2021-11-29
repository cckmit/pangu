package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class DmgUpOnCounterParam {
    /** 每层增伤率*/
    private double ratePerCount;

    /** 计数器tag*/
    private String buffTag;
}
