package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class MoRiShenPanMeteorParam {

    private DamageParam crashDmg;

    private StateAddParam state;

    private int radius;


    private DamageParam dotDmg;

    private int length;

    private int width;

    private int exeTimes = 3;

    private int exeInterval = 1000;
}
