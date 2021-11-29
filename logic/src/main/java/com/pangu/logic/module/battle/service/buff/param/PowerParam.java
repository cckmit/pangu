package com.pangu.logic.module.battle.service.buff.param;

import lombok.Getter;

@Getter
public class PowerParam {
    //提升的Tag
    private String tag;

    //需要大于的比率
    private double rate;

    //提升的比率
    private double increaseRate;

    //存在Tag的情况数值
    private double tagRate;
}
