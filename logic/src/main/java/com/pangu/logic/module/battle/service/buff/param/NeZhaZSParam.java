package com.pangu.logic.module.battle.service.buff.param;

import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import lombok.Getter;

import java.util.Map;

@Getter
public class NeZhaZSParam {
    
    private String counterTag;

    
    private Map<Integer,Double> totalCount2CureRate;

    
    private StateAddParam stateAddParam;
    
    private String deBuff;
    
    private int stateTriggerCount;
}
