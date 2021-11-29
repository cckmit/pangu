package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class ZiRanZhiNuParam {
    //目标数量
    private int count;
    //是否可攻击已命中的目标
    private boolean repeatable;
    //伤害参数
    private DamageParam dmgParam;
}