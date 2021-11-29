package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class ShenPanGangFengParam {
    /** 路径范围*/
    private int width;
    /** 路径伤害*/
    private DamageParam roadDmg;

    /** 爆炸范围*/
    private int radius;
    /** 爆炸伤害*/
    private DamageParam circleDmg;
    /** 爆炸添加异常*/
    private StateAddParam stateAddParam;
}
