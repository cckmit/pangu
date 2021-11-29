package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class DecreaseOverflowDamageParam {
    //当前伤害高于此血量
    private double hpRate;

    //溢出伤害减少此比率
    private double decreaseRate;
}
