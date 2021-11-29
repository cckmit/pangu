package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class DamageDeepenParam {

    // 触发几率
    private double rate;

    // 使用公式判断
    private String expression;

    private double enhanceValue;
}
