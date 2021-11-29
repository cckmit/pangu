package com.pangu.logic.module.battle.model.report.values;

import com.pangu.framework.protocol.annotation.Transable;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 闪避
 */
@Transable
@Data
@NoArgsConstructor
public class Miss implements IValues {
    @Getter(AccessLevel.PRIVATE)
    private ValuesType type = ValuesType.MISS;
    private int value;

    public Miss(int value) {
        this.value = value;
    }
}
