package com.pangu.logic.module.battle.model.report.values;

import com.pangu.logic.module.battle.model.Point;
import com.pangu.framework.protocol.annotation.Transable;
import lombok.*;

/**
 * 场上道具
 */
@Transable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ItemAdd implements IValues {
    @Getter(AccessLevel.PRIVATE)
    private ValuesType type = ValuesType.ITEM_ADD;

    /**
     * 道具ID
     */
    private int id;

    /**
     * 位置
     */
    private Point point;

    /**
     * 失效时间
     */
    private int invalidTime;

    public ItemAdd(int id, Point point, int invalidTime) {
        this.id = id;
        this.point = new Point(point);
        this.invalidTime = invalidTime;
    }
}
