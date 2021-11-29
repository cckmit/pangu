package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import lombok.Getter;

@Getter
public class ThunderHitParam {
    //  X次普攻触发
    private int triggerCount;

    //  增伤率
    private double dmgUpRate;

    //  添加异常
    private StateAddParam stateAddParam;

    //  是否开启击杀奖励
    private boolean killBonus;
}
