package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

import java.util.Map;

@Getter
public class AdditionalEffectByCounterParam {
    /** 从目标身上指定tag的counter buff中获取计数层数*/
    private String counterTag;

    /** 特定技能-计数层数才会触发*/
    private Map<String,Integer> triggerTagToTriggerCount;

    /** 触发 debuff添加 行为的技能tag*/
    private String deBuffTriggerTag;
    private String deBuffId;

    /** 触发 即死 行为的技能tag*/
    private String deathTriggerTag;
}
