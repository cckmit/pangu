package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class LowHpAddBuffsParam {
    //低于血量的百分比
    private double hpRate;
    //添加的BUFF
    private String[] buffs;
}
