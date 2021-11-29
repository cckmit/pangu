package com.pangu.logic.module.battle.service.buff.param;

import lombok.Getter;

@Getter
public class ValDrainParam {
    //  属性扣减表达式
    private DefaultAddValueParam drainedVal;

    //  属性增加表达式
    private DefaultAddValueParam drainingVal;
}
