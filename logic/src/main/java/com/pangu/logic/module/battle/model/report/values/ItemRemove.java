package com.pangu.logic.module.battle.model.report.values;

import com.pangu.framework.protocol.annotation.Transable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 场上道具
 */
@Transable
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ItemRemove implements IValues {
    @Getter(AccessLevel.PRIVATE)
    private ValuesType type = ValuesType.ITEM_REMOVE;

    /**
     * 道具ID
     */
    private int id;

    public ItemRemove(int id) {
        this.id = id;
    }
}
