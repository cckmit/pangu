package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class HuaDiWeiLaoParam {

    
    private String passiveId;
    
    @Deprecated
    private String buffId;

    
    private int disableDuration = 5000;
}
