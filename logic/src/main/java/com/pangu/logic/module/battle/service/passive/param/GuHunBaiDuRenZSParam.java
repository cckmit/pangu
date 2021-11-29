package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class GuHunBaiDuRenZSParam {

    /** 每100点能量对应的治疗率提升比率 */
    private double cureUpRate;
    /** 治疗率提升上限 */
    private double cureUpLimit;
    /** 能量提升所需的治疗量百分比 */
    private double mpUpHpPct;
    /** 能量提升值 */
    private int mpUpValue;

}
