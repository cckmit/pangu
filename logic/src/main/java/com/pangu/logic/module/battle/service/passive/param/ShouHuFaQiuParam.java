package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import lombok.Getter;

@Getter
public class ShouHuFaQiuParam {
    //击退距离
    private int distance;

    //触发时的目标选择策略
    private String targetId;

    //伤害参数
    private DamageParam damageParam;
}
