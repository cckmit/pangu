package com.pangu.logic.module.battle.model.report.values;

import com.pangu.framework.protocol.annotation.Transable;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 免疫
 */
@Transable
@Data
@NoArgsConstructor
public class Immune implements IValues {
    @Getter(AccessLevel.PRIVATE)
    private ValuesType type = ValuesType.IMMUNE;

    private int value;

    public Immune(int value) {
        this.value = value;
    }
}
