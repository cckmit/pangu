package com.pangu.logic.module.battle.service.select.select;

import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.select.utils.Rectangle;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * +-------+
 * |       |
 * |       |
 * |   p   |
 * |       |
 * |       |
 * +-------+
 * <p>
 * 平行于X轴的矩形，owner位于矩形内部中心。长为x轴坐标，宽为y轴坐标
 */
public class HorizontalEnclosingRectangleSelect implements Selector {
    @Override
    public List<Unit> select(Unit owner, List<Unit> units, SelectSetting selectSetting, int time) {
        if (CollectionUtils.isEmpty(units)) {
            return Collections.emptyList();
        }

        //获取矩形长宽
        final int width = selectSetting.getWidth();
        final int length = selectSetting.getDistance();

        //获取用于固定矩形的关键坐标
        final Point ownerPoint = owner.getPoint();
        final Point targetPoint1 = new Point(BattleConstant.MAX_X, ownerPoint.y);
        final Point targetPoint2 = new Point(0, ownerPoint.y);

        //构建矩形
        final Rectangle rectangle1 = new Rectangle(ownerPoint, targetPoint1, width, length, 0);
        final Rectangle rectangle2 = new Rectangle(ownerPoint, targetPoint2, width, length, 0);

        //筛选位于矩形中的目标
        final ArrayList<Unit> targetsInRect1 = new ArrayList<>(units.size());
        final ArrayList<Unit> targetsInRect2 = new ArrayList<>(units.size());
        for (Unit unit : units) {
            final int x = unit.getPoint().x;
            final int y = unit.getPoint().y;
            if (rectangle1.inRect(x, y)) {
                targetsInRect1.add(unit);
            }
            if (rectangle2.inRect(x, y)) {
                targetsInRect2.add(unit);
            }
        }
        return targetsInRect1.size() >= targetsInRect2.size() ? targetsInRect1 : targetsInRect2;
    }
}
