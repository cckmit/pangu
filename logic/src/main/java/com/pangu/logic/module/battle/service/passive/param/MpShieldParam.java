package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class MpShieldParam {
    //每次伤害最大可使用MP抵消的量
    private int maxMpShield;
    //触发条件：单次伤害值占生命上限的比例
    private double triggerRate;
}
