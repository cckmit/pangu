package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class MiZongZhiDieParam {

    /** 触发的技能标识 */
    private String skillId;
    /** 冷却时间 */
    private int cd;
    /** 每次受伤降低的冷却时间 */
    private int preCdDec;

}
