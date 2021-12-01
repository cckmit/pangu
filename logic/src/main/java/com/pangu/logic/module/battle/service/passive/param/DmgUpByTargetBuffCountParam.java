package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class DmgUpByTargetBuffCountParam {
    
    private String triggerTag;

    
    private String buffClassify;

    
    private double dmgUpRatePerBuffCount;

    
    private double maxDmgUpRate;
}
