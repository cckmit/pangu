package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class AttackDeepenByTargetHpParam {
    //每少多少血（百分比）
    private double preHpRate;

    //提升多少伤害
    private double increaseRate;
}
