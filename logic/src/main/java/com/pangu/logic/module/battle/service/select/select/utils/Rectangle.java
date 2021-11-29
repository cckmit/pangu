package com.pangu.logic.module.battle.service.select.select.utils;

import com.pangu.logic.module.battle.model.Point;

/**
 * * P4       P3
 * * +-------+
 * * |       |
 * * |       |
 * * |       |
 * * |       |
 * * +---P---+
 * * P1       P2
 * 矩形范围
 */
public class Rectangle {

    private static final int PROBABLE = 5;

    private final Point[] ps;

    public Rectangle(Point[] ps) {
        this.ps = ps;
    }

    public Rectangle(Point start, Point target, int width, int length) {
        this.ps = buildRectangle(start, target, width, length, null);
    }

    public Rectangle(Point start, Point target, int width, int length, int probable) {
        this.ps = buildRectangle(start, target, width, length, probable);
    }

    /**
     * 相对于X坐标系X正方向角度
     *
     * @param zeroPoint 坐标0点
     * @param target
     * @return
     */
    public static double calRadiansByX(Point zeroPoint, Point target) {
        // 向量角度公式 a.b = |a| |b| cos$ 等价于  $ = acos((x1 * x2 + y1 * y2) / (sqrt((x1-x2) * (x1- x2) + y)))
        Point start = new Point(1, 0);
        Point end = new Point(target.getX() - zeroPoint.getX(), target.getY() - zeroPoint.getY());
        int dot = start.getX() * end.getX() + start.getY() * end.getY();
        double len = (Math.sqrt(start.getX() * start.getX() + start.getY() * start.getY()) * Math.sqrt(end.getX() * end.getX() + end.getY() * end.getY()));
        return Math.acos(dot / len);
    }

    public static Point[] buildRectangle(Point start, Point target, int width, int length, Integer probable) {
        double radians = calRadiansByX(start, target);
        int halfWidth = width / 2;

        if (probable == null) {
            probable = PROBABLE;
        }

        // X坐标提供误差，防止算出来的不是一条直线

        Point p1 = new Point(start.getX() - probable, start.getY() + halfWidth);
        Point p2 = new Point(start.getX() - probable, start.getY() - halfWidth);
        Point p3 = new Point(start.getX() + length + probable, start.getY() - halfWidth);
        Point p4 = new Point(start.getX() + length + probable, start.getY() + halfWidth);

        p1 = rotate(start, p1, target, radians);
        p2 = rotate(start, p2, target, radians);
        p3 = rotate(start, p3, target, radians);
        p4 = rotate(start, p4, target, radians);
        return new Point[]{p1, p2, p3, p4};
    }

    public Point[] getPs() {
        return ps;
    }

    public boolean inRect(int x, int y) {
        Point p = new Point(x, y);
        return GetCross(ps[0], ps[1], p) * GetCross(ps[2], ps[3], p) >= 0 && GetCross(ps[1], ps[2], p) * GetCross(ps[3], ps[0], p) >= 0;
    }

    private float GetCross(Point p1, Point p2, Point p) {
        return (p2.getX() - p1.getX()) * (p.getY() - p1.getY()) - (p.getX() - p1.getX()) * (p2.getY() - p1.getY());
    }

    private static Point rotate(Point start, Point p1, Point target, double degrees) {
//        如果是逆时针旋转：
//        x2 = (x1 - x0) * cosa - (y1 - y0) * sina + x0
//        y2 = (y1 - y0) * cosa + (x1 - x0) * sina + y0
//        如果是顺时针旋转：
//        x2 = (x1 - x0) * cosa + (y1 - y0) * sina + x0
//        y2 = (y1 - y0) * cosa - (x1 - x0) * sina + y0
        if (target.getY() > start.getY()) {
            int x2 = (int) ((p1.getX() - start.getX()) * Math.cos(degrees) - (p1.getY() - start.getY()) * Math.sin(degrees) + start.getX());
            int y2 = (int) ((p1.getY() - start.getY()) * Math.cos(degrees) + (p1.getX() - start.getX()) * Math.sin(degrees) + start.getY());
            return new Point(x2, y2);
        } else {
            int x2 = (int) ((p1.getX() - start.getX()) * Math.cos(degrees) + (p1.getY() - start.getY()) * Math.sin(degrees) + start.getX());
            int y2 = (int) ((p1.getY() - start.getY()) * Math.cos(degrees) - (p1.getX() - start.getX()) * Math.sin(degrees) + start.getY());
            return new Point(x2, y2);
        }
    }

    public Point calMidPointOnOppositeSide() {
        final Point point = new Point();
        point.x = (ps[2].x + ps[3].x) / 2;
        point.y = (ps[2].y + ps[3].y) / 2;
        return point;
    }

}
