package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class QiKaiParam {

    /** 触发概率 */
    private double rate;
    /** 伤害比率 */
    private double damagePct;
    /** 目标选择 */
    private String selectId;
    /** 伤害上限（攻击力百分比） */
    private double dmgLimit;

    /** 普攻指定次数后下次受击必然触发*/
    private int triggerNormalAtkTimes = Integer.MAX_VALUE;

    /** 添加的debuff*/
    private String deBuff;

    /** 添加次数上限*/
    private int deBuffLimit;
}
