package com.pangu.logic.module.battle.model.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.framework.protocol.annotation.Transable;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * 移动战报
 */
@JsonInclude(Include.NON_NULL)
@Transable
@Getter
public class MoveReport implements IReport {

    @Getter(AccessLevel.PRIVATE)
    private ReportType type = ReportType.MOVE;

    /**
     * 执行时间
     */
    private int time;

    /**
     * 所有者
     */
    private String owner;

    /**
     * 花费时间
     */
    private int costTime;

    /**
     * x坐标
     */
    private int x;

    /**
     * y坐标
     */
    private int y;

    /**
     * 构造
     *
     * @param owner
     * @return
     */
    public static MoveReport of(int time, Unit owner, int costTime) {
        MoveReport report = new MoveReport();
        report.time = time;
        report.owner = owner.getId();
        Point point = owner.getPoint();
        report.x = point.x;
        report.y = point.y;
        report.costTime = costTime;
        return report;
    }
}
