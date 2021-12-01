package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import lombok.Getter;

@Getter
public class FanJiZhiLianParam {
    @Deprecated
    private String effectId;


    private String buffId;


    private DamageParam dmg;


    private int repelDist = 45;


    private double triggerRate;
}
