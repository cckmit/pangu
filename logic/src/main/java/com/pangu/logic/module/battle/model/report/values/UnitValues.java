package com.pangu.logic.module.battle.model.report.values;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.framework.protocol.annotation.Transable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Transable
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UnitValues implements IValues {
    @Getter(AccessLevel.PRIVATE)
    private ValuesType type = ValuesType.UNIT_VALUE;

    /**
     * 类型
     */
    private AlterType alterType;

    /**
     * 值
     */
    private Number value;

    public UnitValues(AlterType alterType, Number value) {
        this.alterType = alterType;
        this.value = value;
    }
}
