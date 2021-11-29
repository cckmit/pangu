package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class DianCiLiChangParam {
    //伤害倍率
    private double factor;
    //伤害治疗转化比例
    private double damageToCureRate;
    //提前生效的治疗比例
    private double hungryCureRate;


    @Deprecated
    private CurePhase curePhase;
    public enum CurePhase{
        //每次伤害治疗
        DAMAGE_ONCE,
        //技能结束后一次性治疗
        DAMAGE_RUN_OUT
    }

}
