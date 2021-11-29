package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class ManNiuXueMaiParam {

    /**
     * 触发比率
     */
    private double activeRate;

    /**
     * 抵挡所有伤害被动
     */
    private String passiveId;
}
