package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

import java.util.Map;

@Getter
public class BuffCastOnIndirectlyKillParam {
    
    private Map<String,String> buff2ConExp;
    
    private String target;

    
    private boolean needDirectKill;
}
