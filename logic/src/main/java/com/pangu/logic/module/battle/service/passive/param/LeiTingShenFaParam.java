package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class LeiTingShenFaParam {
    //最大增伤比例上限
    private double maxRate = Double.MAX_VALUE;
    //增伤比例
    private double ratePerEnemy;

    //负相关，为true则每减少一个敌人增加伤害
    //为false则每增加一个敌人增加伤害
    private boolean negative;
    //范围内存在X人时，不加伤害
    private int baseCount;

    // 当目标存在特殊状态时
    private double factorForShieldTarget;
}
