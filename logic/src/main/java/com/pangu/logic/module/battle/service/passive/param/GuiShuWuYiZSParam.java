package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

import java.util.Collections;
import java.util.Set;

@Getter
public class GuiShuWuYiZSParam {

    /** 指定的技能标签 */
    private Set<String> skillTags = Collections.emptySet();
    /** 图腾单元配置标识 */
    private String unitId;
    /** 攻击力对应血量比率 */
    private double hpRate;
    /** 伤害系数 */
    private double factor;
    /** 伤害范围目标 */
    private String selectId;
    /** 图腾回血BUFF标识 */
    private String cureBuffId;
    /** 嘲讽持续时间 */
    private int sneerTime;
}
