package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class AnYingMoZhuZSParam {

    /** 替换的技能标签 */
    private String skillTag;
    /** 替换的技能cd*/
    private int cd = 1;
    /** 弱化系数 */
    private double weakFactor;
    /** 每普攻X次后替换技能 */
    private int normalTimes;
    /** 伤害加成率 */
    private double dmgRate;
    /** 伤害加成持续时间 */
    private int dmgContinueTime = Integer.MAX_VALUE;
    /** 伤害加成叠加次数上限 */
    private int overlayLimit;

}
