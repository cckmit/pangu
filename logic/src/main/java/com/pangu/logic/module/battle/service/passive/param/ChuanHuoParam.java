package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class ChuanHuoParam {

    /** 触发概率 */
    private double rate;
    /** 伤害范围目标选择标识 */
    private String selectId;
    /** 伤害系数 */
    private double factor;
    /** 暴击提升率 */
    private double critUpRate;

}
