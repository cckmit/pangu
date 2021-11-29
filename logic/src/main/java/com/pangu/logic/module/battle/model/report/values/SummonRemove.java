package com.pangu.logic.module.battle.model.report.values;

import com.pangu.framework.protocol.annotation.Transable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 召唤单位移除
 */
@Transable
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SummonRemove implements IValues {

    @Getter(AccessLevel.PRIVATE)
    private ValuesType type = ValuesType.SUMMON_REMOVE;

    /**
     * 移除的单位ID集合
     */
    private List<String> unitIds;

    public SummonRemove(List<String> unitIds) {
        this.unitIds = unitIds;
    }
}
