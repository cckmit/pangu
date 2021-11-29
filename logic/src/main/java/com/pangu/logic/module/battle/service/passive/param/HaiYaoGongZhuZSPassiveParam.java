package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class HaiYaoGongZhuZSPassiveParam {

    /** BUFF标识 */
    private String buffId;
    /** 分身叠加上限 */
    private int summonOverlayTimes;
    /** 恢复层数 */
    private int recoverOverlayTimes;
    /** 回复cd*/
    private int recoverCd = 30000;

    /** 免疫伤害所需比率 */
    private double immunePct;

}
