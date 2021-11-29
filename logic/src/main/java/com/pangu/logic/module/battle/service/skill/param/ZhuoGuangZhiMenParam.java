package com.pangu.logic.module.battle.service.skill.param;


import lombok.Getter;


@Getter
public class ZhuoGuangZhiMenParam {
    //移动延迟
    private int moveDelay;

    //伤害参数
    private DamageParam dmg;

    //伤害半径
    private int dmgRadius;
}
