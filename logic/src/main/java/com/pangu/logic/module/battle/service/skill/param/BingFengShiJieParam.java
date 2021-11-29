package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class BingFengShiJieParam {
    //首次循环时的伤害参数
    private DamageParam releaseDmg;
    //首次循环时添加的Debuff
    private String deBuff;

    //后续dot伤害参数
    private DamageParam dotDmg;
}
