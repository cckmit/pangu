package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.model.UnitType;
import lombok.Getter;

import java.util.Collections;
import java.util.Set;

@Getter
public class XingChenSheShouZSParam {

    
    private double hitUpRate;
    
    private Set<UnitType> harmUpProfessions = Collections.emptySet();
    
    private double harmUpRate;
    
    private double harmUpLimit;

}
