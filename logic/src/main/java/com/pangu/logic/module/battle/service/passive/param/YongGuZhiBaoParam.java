package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class YongGuZhiBaoParam {
    /** 分摊伤害的目标中必须包含特定的buffTag*/
    private String withBuffTag;
    /** 分摊的生命值百分比上限*/
    private double hpPct;
}
