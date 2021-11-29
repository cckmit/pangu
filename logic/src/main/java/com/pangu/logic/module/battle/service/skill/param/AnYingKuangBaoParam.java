package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnYingKuangBaoParam {

    // 伤害参数
    private double factor;

    // 伤害增加系数
    private double damageEnhanceRate;

    // cd持续时间
    private int continueTime;

    // 效果叠加层数
    private int timesLimit;

    public AnYingKuangBaoParam copy() {
        AnYingKuangBaoParam param = new AnYingKuangBaoParam();
        param.factor = factor;
        param.damageEnhanceRate = damageEnhanceRate;
        param.continueTime = continueTime;
        param.timesLimit = timesLimit;
        return param;
    }
}
