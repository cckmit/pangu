package com.pangu.logic.module.battle.model.report;

import com.pangu.framework.protocol.annotation.Transable;

/**
 * 战报类型
 */
@Transable
public enum ReportType {

    /**
     * 移动
     */
    MOVE,

    /**
     * buff
     */
    BUF,

    /**
     * 定时怒气增加
     */
    MP,

    /**
     * 技能战报
     */
    SKILL,
    ;
}
