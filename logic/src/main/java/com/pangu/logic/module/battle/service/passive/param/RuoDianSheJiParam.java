package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class RuoDianSheJiParam {
    private int triggerCount;

    //当目标身上存在负面状态时所添加的层数
    private int countAddWhenDeBuff = 2;
    private boolean deBuffBonus;

    //暴击率提升
    private double critProbUp = 10;
}
