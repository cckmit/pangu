package com.pangu.logic.module.battle.model.report.values;

import com.pangu.framework.protocol.annotation.Transable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Transable
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RemovePassive implements IValues{
    @Getter(AccessLevel.PRIVATE)
    private ValuesType type = ValuesType.REMOVE_PASSIVE;

    /**
     * 被动ID
     */
    private String passive;

    public RemovePassive(String passive) {
        this.passive = passive;
    }
}
