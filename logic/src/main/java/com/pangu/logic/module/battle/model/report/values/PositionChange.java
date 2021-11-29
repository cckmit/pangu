package com.pangu.logic.module.battle.model.report.values;

import com.pangu.framework.protocol.annotation.Transable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 位置变更
 */
@Transable
@Getter
@Setter
@NoArgsConstructor
public class PositionChange implements IValues {
    @Getter(AccessLevel.PRIVATE)
    private ValuesType type = ValuesType.POSITION;

    private int x;

    private int y;

    /** 位置变更结束时的时刻，仅供前端表现。实际位移早已完成*/
    private int stopTime;

    public PositionChange(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static PositionChange of(int x, int y) {
        PositionChange r = new PositionChange();
        r.x = x;
        r.y = y;
        return r;
    }
}
