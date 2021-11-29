package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class ChangeDmgWhenNonCounterListenerParam {
    /** 伤害调整率*/
    private double hpChangeRate;

    /** 监听方向*/
    private boolean ownerListenFriend;
}
