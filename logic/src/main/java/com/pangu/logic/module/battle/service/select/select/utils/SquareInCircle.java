package com.pangu.logic.module.battle.service.select.select.utils;

import com.pangu.logic.module.battle.model.Point;

/**
 * 计算圆形范围内部的正方形坐标
 */
public class SquareInCircle {

    private static final double COST_45 = 0.70710678118;

    public static Point[] square(Point circle, int radius) {
        int halfWidth = (int) (radius * COST_45);
        int x = circle.x;
        int y = circle.y;
        return new Point[]{new Point(x - halfWidth, y + halfWidth),
                new Point(x + halfWidth, y + halfWidth),
                new Point(x + halfWidth, y - halfWidth),
                new Point(x - halfWidth, y - halfWidth)};
    }
}
