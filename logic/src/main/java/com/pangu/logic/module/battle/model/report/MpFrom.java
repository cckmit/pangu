package com.pangu.logic.module.battle.model.report;

import com.pangu.framework.protocol.annotation.Transable;

/**
 * 怒气变更来源
 */
@Transable
public enum MpFrom {

    /**
     * 技能使用(大招扣减怒气，普攻增加怒气)
     */
    SKILL,

    /**
     * 相关技能效果变更怒气
     */
    NORMAL,

    /**
     * 击杀
     */
    KILL,

    /**
     * 受伤增加怒气
     */
    DAMAGE,

    /**
     * 定时增加
     */
    INTERVAL,
    ;

}
