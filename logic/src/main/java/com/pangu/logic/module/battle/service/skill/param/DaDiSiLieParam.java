package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

/**
 * 通用矩形区域伤害参数
 */
@Getter
public class DaDiSiLieParam {
    private DamageParam dmg;

    //矩形宽
    private int width;

    //矩形长
    private int length;

    //矩形内部小矩形的宽度
    private int innerWidth;
}
