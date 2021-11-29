package com.pangu.logic.module.battle.model.report;

import com.pangu.framework.protocol.annotation.Transable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 定时增加怒气战报
 */
@Transable
@Getter
@NoArgsConstructor
public class MpReport implements IReport {

    @Getter(AccessLevel.PRIVATE)
    private ReportType type = ReportType.MP;

    /**
     * 执行时间
     */
    private int time;

    /**
     * id->mp
     */
    private Map<String, Long> mp;

    public MpReport(int time, Map<String, Long> mp) {
        this.time = time;
        this.mp = mp;
    }
}
