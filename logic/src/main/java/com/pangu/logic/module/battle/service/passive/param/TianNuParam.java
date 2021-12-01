package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import lombok.Getter;

@Getter
public class TianNuParam extends DamageParam {

    private String target;


    private double triggerRate;
}
