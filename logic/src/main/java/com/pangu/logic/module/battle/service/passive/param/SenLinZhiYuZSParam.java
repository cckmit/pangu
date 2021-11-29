package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class SenLinZhiYuZSParam {

    /** 治疗系数 */
    private double factor;
    /** 普攻暴击时提升的治疗率 */
    private double critUpCureRate;
    /** 治疗目标 */
    private String selectId;
}
