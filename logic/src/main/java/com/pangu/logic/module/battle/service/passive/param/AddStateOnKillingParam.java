package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import lombok.Getter;

@Getter
public class AddStateOnKillingParam {
    
    private StateAddParam stateParam;
    
    private String target;

    
    private String skillTag;

    
    private boolean selfKilling = true;
}
