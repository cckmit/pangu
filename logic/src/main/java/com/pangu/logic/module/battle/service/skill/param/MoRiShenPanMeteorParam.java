package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class MoRiShenPanMeteorParam {
    /** 陨石撞击伤害*/
    private DamageParam crashDmg;
    /** 陨石撞击添加的异常状态*/
    private StateAddParam state;
    /** 陨石撞击半径*/
    private int radius;

    /** 灼烧持续伤害*/
    private DamageParam dotDmg;
    /** 灼烧地形长度*/
    private int length;
    /** 灼烧地形宽度*/
    private int width;
    /** 灼烧地形循环执行次数*/
    private int exeTimes = 3;
    /** 灼烧地形循环间隔*/
    private int exeInterval = 1000;
}
