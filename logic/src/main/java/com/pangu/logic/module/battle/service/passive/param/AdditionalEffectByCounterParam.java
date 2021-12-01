package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

import java.util.Map;

@Getter
public class AdditionalEffectByCounterParam {

    private String counterTag;


    private Map<String,Integer> triggerTagToTriggerCount;


    private String deBuffTriggerTag;
    private String deBuffId;


    private String deathTriggerTag;
}
