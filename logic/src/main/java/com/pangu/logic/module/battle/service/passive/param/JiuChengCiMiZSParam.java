package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class JiuChengCiMiZSParam {
    private String deBuff;
    private String buff;
    //攻速提升奖励
    private boolean deBuffBonus;
    //暴击提升率
    private double critUpFactor = 10;
}
