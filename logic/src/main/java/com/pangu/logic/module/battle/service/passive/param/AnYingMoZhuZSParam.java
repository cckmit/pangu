package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class AnYingMoZhuZSParam {

    
    private String skillTag;
    
    private int cd = 1;
    
    private double weakFactor;
    
    private int normalTimes;
    
    private double dmgRate;
    
    private int dmgContinueTime = Integer.MAX_VALUE;
    
    private int overlayLimit;

}
