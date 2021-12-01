package com.pangu.logic.module.battle.service.skill.param;

import com.pangu.logic.module.battle.service.passive.param.BuffUpdateParam;
import lombok.Getter;

@Getter
public class NeZhaTripleAtkParam {
    
    private DamageParam damageParam;

    
    private BuffUpdateParam buffUpdateParam;

    
    private StateAddParam stateAddParam;

    
    private boolean critBonus;
}
