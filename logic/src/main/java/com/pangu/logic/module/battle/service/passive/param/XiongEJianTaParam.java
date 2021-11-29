package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class XiongEJianTaParam {

    // 触发几率
    private double rate;

    // 目标周围的圆形半径
    private int range;

    // 伤害
    private double factor;
}
