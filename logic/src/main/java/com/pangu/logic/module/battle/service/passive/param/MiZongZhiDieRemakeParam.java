package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class MiZongZhiDieRemakeParam {
    /**缩短冷却时间的技能标签*/
    private String skillTag;

    /**每次受击缩短cd时间 ms*/
    private int cdChange;
}
