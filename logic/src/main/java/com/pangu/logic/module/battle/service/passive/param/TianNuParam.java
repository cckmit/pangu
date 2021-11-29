package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import lombok.Getter;

@Getter
public class TianNuParam extends DamageParam {
    /** 所产生范围伤害的目标*/
    private String target;

    /** 触发概率*/
    private double triggerRate;
}
