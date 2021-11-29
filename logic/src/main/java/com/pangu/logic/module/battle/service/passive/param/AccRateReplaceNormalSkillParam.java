package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;
@Getter
public class AccRateReplaceNormalSkillParam {
    //基本触发概率
    private double baseRate;
    //未触发补偿概率
    private double rateIncrease;
    //用于替换普攻的技能
    private String skillId;
}
