package com.pangu.logic.module.battle.model.report.values;

import com.pangu.framework.protocol.annotation.Transable;

/**
 * 值类型
 */
@Transable
public enum ValuesType {
    /**
     * 复活战报
     */
    REVIVE,
    /**
     * 添加BUFF
     */
    BUFF_ADD,
    /**
     * 印记
     */
    MARK,
    /**
     * 变身
     */
    TRANSFORM,

    /**
     * 移除被动
     */
    REMOVE_PASSIVE,

    /**
     * 添加被动
     */
    ADD_PASSIVE,

    /**
     * 血量伤害
     */
    HP,

    /**
     * 免疫伤害
     */
    IMMUNE,

    /**
     * 闪避
     */
    MISS,

    /**
     * 怒气变更
     */
    MP,

    /**
     * 被动生效属性
     */
    PASSIVE,

    /**
     * 位置变更
     */
    POSITION,

    /**
     * 属性变更
     */
    UNIT_VALUE,

    /**
     * 眩晕等状态被移除
     */
    STATE_REMOVE,

    /**
     * 添加眩晕以及其他状态
     */
    STATE_ADD,

    /**
     * 召唤单位
     */
    SUMMON_UNIT,

    /**
     * 移除召唤单位
     */
    SUMMON_REMOVE,

    /**
     * 显示道具
     */
    ITEM_ADD,

    /**
     * 移动道具
     */
    ITEM_MOVE,

    /**
     * 道具移除
     */
    ITEM_REMOVE,

    /**
     * 跟随变更
     */
    FOLLOW,

    /**
     * 直接死亡
     */
    DEATH,
    ;
}
