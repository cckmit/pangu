package com.pangu.logic.module.battle.service.skill.param;

import com.pangu.logic.module.battle.model.Point;
import lombok.Getter;

@Getter
public class TwinsSummonParam {
    /** 将自身指定前缀开头的主动技能添加到召唤物身上*/
    private String skillPrefix;

    /** 召唤物id*/
    private String baseId;

    /** 召唤物变身状态*/
    private int transformState;

    /** 召唤位置*/
    private Point point;

    /** 召唤物移除时间*/
    private int removeTime;

    /** 释放大招的延时*/
    private int lastStrawDelay;

    /** 召唤系数*/
    private double rate;
}
