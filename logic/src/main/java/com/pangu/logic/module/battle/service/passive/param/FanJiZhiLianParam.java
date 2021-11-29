package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import lombok.Getter;

@Getter
public class FanJiZhiLianParam {
    @Deprecated
    private String effectId;

    /** 反击时为目标添加的Debuff*/
    private String buffId;

    /** 反击时造成的伤害*/
    private DamageParam dmg;

    /** 反击击退距离*/
    private int repelDist = 45;

    /** 反击触发率*/
    private double triggerRate;
}
