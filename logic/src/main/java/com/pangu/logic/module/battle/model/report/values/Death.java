package com.pangu.logic.module.battle.model.report.values;

import com.pangu.framework.protocol.annotation.Transable;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * 直接击杀单元
 */
@Getter
@Transable
public class Death implements IValues {
    @Getter(AccessLevel.PRIVATE)
    private ValuesType type = ValuesType.DEATH;
}
