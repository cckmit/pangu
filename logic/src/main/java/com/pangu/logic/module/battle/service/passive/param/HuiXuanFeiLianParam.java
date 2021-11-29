package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class HuiXuanFeiLianParam {
    //触发成功率
    private double triggerRate;
    //触发成功时添加的buff
    private String buffId;
    //触发成功时回复的能量
    private int mp;
}
