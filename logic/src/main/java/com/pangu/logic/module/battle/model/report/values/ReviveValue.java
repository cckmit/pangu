package com.pangu.logic.module.battle.model.report.values;

import com.pangu.framework.protocol.annotation.Transable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 复活
 */
@Transable
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReviveValue implements IValues{
    @Getter(AccessLevel.PRIVATE)
    private ValuesType type = ValuesType.REVIVE;
}
