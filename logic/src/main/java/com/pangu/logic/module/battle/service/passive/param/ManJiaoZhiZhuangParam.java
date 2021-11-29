package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class ManJiaoZhiZhuangParam {

    private double rate;

    private String skillId;

    //触发添加护盾的技能组
    private String[] triggerSkills;
    //强化普攻造成伤害转换护盾的比例
    private double dmgToShieldRate;

}
