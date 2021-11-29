package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class CheckAndReplaceParam {
    //触发条件的选择器
    private String conditionTargetId;
    //需要替换的技能
    private String skillId;
    //true  指定区域内存在敌人时触发
    //false 指定区域内不存在敌人时触发
    private boolean hasUnit;
}
