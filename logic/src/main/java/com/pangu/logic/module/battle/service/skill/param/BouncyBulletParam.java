package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class BouncyBulletParam {
    
    private double spdDecr;
    
    private int minSpd;

    
    private double dmgDecr;
    
    private double minFactor;

    
    private double rangeDecr;
    
    private int minRange;

    
    private int bounceRange;

    
    private DamageParam damageParam;
}
