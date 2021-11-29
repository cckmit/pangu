package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class LieYanTianHuoParam {
    /** 火焰计数器标签*/
    private String counterBuffTag;

    /** 伤害加深率*/
    private double dmgDeepenRate;

    /** 添加的BUFF*/
    private String buffId;

    /** 添加的BUFF种类*/
    private String buffClassify;

    /** 添加的BUFF层数上限*/
    private int buffMaxCount;
}
