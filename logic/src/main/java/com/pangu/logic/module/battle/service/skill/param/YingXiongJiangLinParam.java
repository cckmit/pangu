package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class YingXiongJiangLinParam {

    // 技能效果半径
    private int radius;

    // 伤害比率
    private double factor;

    // 击飞时间
    private int disableTime;

    // 使用攻击力计算的护盾值
    private double shieldByAttack;
}
