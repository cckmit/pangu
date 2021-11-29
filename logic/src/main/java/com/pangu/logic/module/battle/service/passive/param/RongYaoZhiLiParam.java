package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class RongYaoZhiLiParam {

    // 伤害增加比率
    private double damageEnhance;

    // 血量高于多少百分比触发
    private int hpPercentHit;

    // 最多额外造成攻击力*800%伤害
    private double maxAttackEnhance;
}
