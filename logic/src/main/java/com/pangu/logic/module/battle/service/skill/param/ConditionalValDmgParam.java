package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class ConditionalValDmgParam extends ValuesDamageParam {
    /** 触发条件表达式*/
    private String conditionExp;

    /**
     * 一场战斗中是否可重复对同一个目标生效
     */
    private boolean unRepeatableOnSameTarget;
}
