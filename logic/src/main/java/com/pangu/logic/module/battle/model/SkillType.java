package com.pangu.logic.module.battle.model;

import com.pangu.framework.protocol.annotation.Transable;

/**
 * 技能类型
 */
@Transable
public enum SkillType {

    /**
     * 普通攻击
     */
    NORMAL,

    /**
     * 技能攻击
     */
    SKILL,

    /**
     * 大招攻击
     */
    SPACE,

    /**
     * 初始化技能
     */
    INIT,

    /**
     * 羁绊技能
     */
    FATTER,

    /**
     * 其他技能
     */
    OTHER
    ;
}
