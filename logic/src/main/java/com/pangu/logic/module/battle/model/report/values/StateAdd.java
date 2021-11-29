package com.pangu.logic.module.battle.model.report.values;

import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.framework.protocol.annotation.Transable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 状态添加
 */
@Getter
@Transable
@AllArgsConstructor
@NoArgsConstructor
public class StateAdd implements IValues {
    @Getter(AccessLevel.PRIVATE)
    private ValuesType type = ValuesType.STATE_ADD;
    /**
     * 状态类型
     */
    private UnitState state;

    /**
     * 持续到达时间
     */
    private int validTime;

    public StateAdd(UnitState state, int validTime) {
        this.state = state;
        this.validTime = validTime;
    }
}
