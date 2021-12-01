package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class TwinsMeleeSpikeParam {
    
    private String target;
    
    private String lastTarget;

    
    private DamageParam dmgParam;
    
    private DamageParam lastDmgParam;
    
    private int[] intervals;
}
