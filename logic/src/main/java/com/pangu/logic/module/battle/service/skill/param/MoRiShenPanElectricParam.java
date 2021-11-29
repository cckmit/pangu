package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class MoRiShenPanElectricParam {
    private DamageParam dmg;
    private int mpChange;
    private int radius;

    /** 循环次数*/
    private int exeTimes = 3;
    /** 循环间隔*/
    private int exeInterval = 1000;
}
