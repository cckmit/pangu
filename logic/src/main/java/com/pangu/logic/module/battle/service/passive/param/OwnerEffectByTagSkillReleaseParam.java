package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.service.buff.param.DefaultAddValueParam;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import lombok.Getter;

@Getter
public class OwnerEffectByTagSkillReleaseParam {
    
    private String skillTag;

    
    private double rate;

    
    private String targetId;

    
    private String buffId;

    
    private DefaultAddValueParam valModParam;

    
    private DamageParam dmgParam;

    
    private int repelDist;

    
    private StateAddParam stateAddParam;

    
    private String passive;
}
