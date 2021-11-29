package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class FuSuParam {

    /** 血量低于X%时触发 */
    private double hpPct;
    /** 恢复血量比率 */
    private double curePct;

}
