package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class LieMoZhiShiParam {
    //连击达成的阈值
    private int comboCount;
    //最大生命值百分比
    private double hpRate;
    //达成后调用的效果
    private String effectId;
    //攻击倍率伤害上限
    private double atkFactorForMaxDmg;
}
