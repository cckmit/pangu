package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.service.buff.param.DefaultAddValueParam;
import lombok.Getter;

@Getter
public class BuffCastWhenControlledParam {
    
    private String buff;

    
    private double triggerRate = 1;

    
    private DefaultAddValueParam valModParam;

    
    private boolean decontrol;
}
