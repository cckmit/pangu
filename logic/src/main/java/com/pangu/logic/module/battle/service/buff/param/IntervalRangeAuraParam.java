package com.pangu.logic.module.battle.service.buff.param;

import lombok.Getter;

@Getter
public class IntervalRangeAuraParam {

    /** 范围选择标识 */
    private String selectId;
    /** 加BUFF所需间隔时间 */
    private int interval;
    /** 加BUFF标识 */
    private String buffId;
}
