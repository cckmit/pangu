package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class YSSXParam {
    /** 触发时释放的技能id*/
    private String spaceId;

    /** 触发前置次数*/
    private int triggerSpaceTimes;
}
