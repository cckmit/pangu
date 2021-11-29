package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class ShiKongXingZheZSParam {
    /**
     * 囤积伤害率
     */
    private double dmgDepositedRate;

    /**
     * 囤积伤害达标后执行的致死技能
     */
    private String deadlySkill;

    /**
     * 击杀奖励的护盾值计算依据
     */
    private double atkFactor;

    /**
     * 时空行者普攻ID，用于区分被击杀的目标是否为时空行者
     */
    private String normalSkillId;

    /**
     * 击杀时空行者后，无敌持续时长
     */
    private int wudiDur;
}
