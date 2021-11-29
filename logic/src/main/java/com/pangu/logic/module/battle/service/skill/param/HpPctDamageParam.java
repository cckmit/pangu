package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class HpPctDamageParam {

    /** 比率 */
    private double rate;
    /** 伤害上限公式 */
    private String limitExpr;
}
