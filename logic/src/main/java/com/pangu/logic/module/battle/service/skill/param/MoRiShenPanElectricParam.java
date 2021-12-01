package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class MoRiShenPanElectricParam {
    private DamageParam dmg;
    private int mpChange;
    private int radius;


    private int exeTimes = 3;

    private int exeInterval = 1000;
}
