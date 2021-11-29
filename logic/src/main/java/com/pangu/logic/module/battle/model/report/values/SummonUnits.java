package com.pangu.logic.module.battle.model.report.values;

import com.pangu.logic.module.battle.model.UnitInfo;
import com.pangu.framework.protocol.annotation.Transable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 召唤单位集合
 */
@Transable
@Getter
@NoArgsConstructor
public class SummonUnits implements IValues {

    @Getter(AccessLevel.PRIVATE)
    private ValuesType type = ValuesType.SUMMON_UNIT;

    /**
     * 召唤单位列表
     */
    private List<UnitInfo> units;

    /** 0:正常召唤物     1:源石神像      2:主神试炼*/
    @Setter
    private int unitType;

    public SummonUnits(List<UnitInfo> units) {
        this.units = units;
    }
}
