package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class YanMieMoYanParam {
    /** 主要伤害参数*/
    private DamageParam damageParam;

    /** 额外伤害触发率*/
    private double addDmgTriggerRate;
    /** 额外伤害表达式*/
    private String addDmgExp;

    /** 击退触发率*/
    private double otherBonusTriggerRate;
    /** 击退距离*/
    private int repelDistance;
    /** 击退成功时为目标添加的debuff*/
    private String deBuff;
    /** 击退成功时回复的能量*/
    private int mpChange;
    private String mpChangeTarget;
}
