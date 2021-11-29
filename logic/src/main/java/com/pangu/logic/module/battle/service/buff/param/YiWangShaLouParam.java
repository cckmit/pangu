package com.pangu.logic.module.battle.service.buff.param;

import lombok.Getter;

@Getter
public class YiWangShaLouParam {
    /**
     * 添加的MP
     */
    private int mp;

    /**
     * 每有一个友方英雄拥有递减多少
     */
    private double decreasePreFriend;
}
