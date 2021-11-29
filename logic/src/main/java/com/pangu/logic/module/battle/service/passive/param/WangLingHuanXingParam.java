package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class WangLingHuanXingParam {

    // 伤害增加触发几率
    private double damageHitRate;

    // 伤害增加比率
    private double damageEnhance;

    private String skillId;

    // 每当死亡多少个单元，召唤怪物
    private int summonUnitPerDie;
}
