package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class BouncyBulletParam {
    /** 飞行速度衰减率*/
    private double spdDecr;
    /** 飞行速度最多衰减至*/
    private int minSpd;

    /** 伤害衰减率*/
    private double dmgDecr;
    /** 伤害系数最多衰减至 */
    private double minFactor;

    /** 弹射距离衰减率*/
    private double rangeDecr;
    /** 弹射距离最多衰减至*/
    private int minRange;

    /** 弹射范围，该范围内若仅有一个目标则不弹射*/
    private int bounceRange;

    /** 伤害参数*/
    private DamageParam damageParam;
}
