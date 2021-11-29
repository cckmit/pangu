package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class DmgUpByTargetBuffCountParam {
    /** 触发本效果的技能标签*/
    private String triggerTag;

    /** 目标身上具备此分类的BUFF时，才会增伤*/
    private String buffClassify;

    /** 每层buff增伤率*/
    private double dmgUpRatePerBuffCount;

    /** 总增伤率*/
    private double maxDmgUpRate;
}
