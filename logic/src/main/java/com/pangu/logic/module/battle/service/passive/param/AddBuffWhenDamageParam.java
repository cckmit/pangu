package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class AddBuffWhenDamageParam {

    
    private List<String> buffs = Collections.emptyList();

    
    private double prob = 1;

    
    private String targetId;

    public static String MURDERER = "MURDERER";
}
