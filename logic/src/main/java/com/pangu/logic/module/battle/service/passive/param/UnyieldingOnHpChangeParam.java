package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import lombok.Getter;

@Getter
public class UnyieldingOnHpChangeParam {
    /** 添加一个buff*/
    private String buffId;

    /** 无敌持续时间*/
    private int duration;
}
