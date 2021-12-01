package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class YanMieMoYanParam {

    private DamageParam damageParam;


    private double addDmgTriggerRate;

    private String addDmgExp;


    private double otherBonusTriggerRate;

    private int repelDistance;

    private String deBuff;

    private int mpChange;
    private String mpChangeTarget;
}
