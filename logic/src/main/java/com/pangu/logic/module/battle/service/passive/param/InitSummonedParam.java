package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class InitSummonedParam {
    /** 召唤技能的标签*/
    private String effectId;

    /** 为召唤物添加的buff*/
    private String buff;

    /** 为召唤物添加的被动*/
    private String passive;

    /** 为召唤物添加的初始化技能*/
    private String[] inits;
}
