package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class BaoFengJiangLinDamageParam {

    /** 半径范围 */
    private int radius;
    /** 伤害系数 */
    private double factor;
    /** 最后一次的伤害系数 */
    private double lastFactor;
    /** 最后一次伤害照成的能量损失率 */
    private double lastMpLostPct;

}
