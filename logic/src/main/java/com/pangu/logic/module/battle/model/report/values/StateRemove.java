package com.pangu.logic.module.battle.model.report.values;

import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.framework.protocol.annotation.Transable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 状态移除属性
 */
@Transable
@Getter
@NoArgsConstructor
public class StateRemove implements IValues {
    @Getter(AccessLevel.PRIVATE)
    private ValuesType type = ValuesType.STATE_REMOVE;

    /**
     * 移除的状态
     */
    private List<UnitState> removeStates;

    public StateRemove(List<UnitState> removeStates) {
        this.removeStates = removeStates;
    }
}
