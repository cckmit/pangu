package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class TwinsMeleeSpikeParam {
    /** 一般伤害选择器*/
    private String target;
    /** 最后一击目标选择器*/
    private String lastTarget;

    /** 一般伤害系数*/
    private DamageParam dmgParam;
    /** 最后一击伤害系数*/
    private DamageParam lastDmgParam;
    /** 执行间隔和次数*/
    private int[] intervals;
}
