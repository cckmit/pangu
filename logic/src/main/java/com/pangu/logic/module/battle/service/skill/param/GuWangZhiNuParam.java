package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class GuWangZhiNuParam {

    // 伤害比率
    private double factor;

    // 被动ID，用于统计收到的伤害
    private String passive;

    // 目标选择器
    private String targetId;

    // 收到伤害反伤比率
    private double damageReturnRate;

    // 伤害转成自己生命比率
    private double damageToHpRate;

    // 结算伤害时间延迟
    private int time;
}
