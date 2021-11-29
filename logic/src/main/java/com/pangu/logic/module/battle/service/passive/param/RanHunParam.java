package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class RanHunParam {

    /** 点燃分类标识 */
    private String burnClassify;
    /** 眩晕触发概率 */
    private double triggerRate;
    /** 眩晕持续时间 */
    private int duration;
    /** 能量恢复值 */
    private int mp;

}
