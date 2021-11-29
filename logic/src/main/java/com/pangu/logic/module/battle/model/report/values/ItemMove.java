package com.pangu.logic.module.battle.model.report.values;

import com.pangu.logic.module.battle.model.Point;
import com.pangu.framework.protocol.annotation.Transable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Transable
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ItemMove implements IValues{
    @Getter(AccessLevel.PRIVATE)
    private ValuesType type = ValuesType.ITEM_MOVE;

    /**
     * 道具ID
     */
    private int id;

    /**
     * 位置
     */
    private Point point;

    public ItemMove(int id, Point point) {
        this.id = id;
        this.point = new Point(point);
    }
}
