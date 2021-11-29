package com.pangu.logic.module.battle.service.select.select.utils;

import com.pangu.logic.module.battle.model.Point;

import java.util.List;

import static java.lang.Math.sqrt;

/**
 * 以坐标列表中的两个点，以及半径，确定一个圆形，这个圆形包含最多的目标点
 */
public class BestCircle {
    public static Point calBestPoint(Point ownerPoint, List<Point> points, int range, int maxX, int maxY, boolean forMove) {
        int size = points.size();

        if (size == 0) {
            return ownerPoint;
        }

        if (size == 1) {
            if (forMove){
                return calLinePoint(ownerPoint, points.get(0), range);
            } else {
                final Point targetPoint = points.get(0);
                return new Point(targetPoint.getX(), targetPoint.getY());
            }
        }

        int maxAmount = 1;
        Point targetPoint = null;
        OUTER:
        for (int first = 0; first < size - 1; ++first) {
            for (int second = first + 1; second < size; ++second) {
                Point p1 = points.get(first);
                Point p2 = points.get(second);
                if (p1.distance(p2) > (range * 2)) {
                    continue;
                }
                // 计算一个小2格的圆，防止误差点
                Point[] circleCenterPoint = calCircleCenter(p1, p2, range - 2, forMove);
                for (Point point : circleCenterPoint) {
                    if (invalid(maxX, maxY, point)) {
                        continue;
                    }
                    int amount = 2;
                    Circle circle = new Circle(point.x, point.y, range);
                    for (int cur = 0; cur < size; ++cur) {
                        if (cur == first || cur == second) {
                            continue;
                        }
                        Point checkPoint = points.get(cur);
                        if (circle.inShape(checkPoint.x, checkPoint.y)) {
                            ++amount;
                        }
                    }
                    if (amount > maxAmount) {
                        targetPoint = point;
                        maxAmount = amount;
                        if (amount >= size) {
                            break OUTER;
                        }
                    }
                }
            }
        }

        if (targetPoint == null) {
            if (forMove) {
                return calLinePoint(ownerPoint, points.get(0), range);
            } else {
                final Point point = points.get(0);
                return new Point(point.getX(), point.getY());
            }
        }

        return targetPoint;
    }

    private static boolean invalid(int maxX, int maxY, Point point) {
        return point.x <= 0 || point.x > maxX || point.y <= 0 || point.y > maxY;
    }

    public static Point[] calCircleCenter(Point p1, Point p2, int dRadius, boolean forMove) {
        if (forMove) {
            Point center1 = new Point(), center2 = new Point();
            if (p1.x == p2.x) {
                center1.y = (p1.y + p2.y) / 2;
                center2.y = center1.y;
                double directHeight = sqrt(dRadius * dRadius - (p1.y - p2.y) * (p1.y - p2.y) / 4.0);
                center1.x = (int) (p1.x + directHeight);
                center2.x = (int) (p2.x - directHeight);
                return new Point[]{center1, center2};
            }
            if (p1.y == p2.y) {
                center1.x = (p1.x + p2.x) / 2;
                center2.x = center1.x;
                double directHeight = sqrt(dRadius * dRadius - (p1.x - p2.x) * (p1.x - p2.x) / 4.0);
                center1.y = (int) (p1.y + directHeight);
                center2.y = (int) (p2.y - directHeight);
                return new Point[]{center1, center2};
            }
            double k = 1.0 * (p2.y - p1.y) / (p2.x - p1.x);
            double k_verticle = -1.0 / k;

            double mid_x = (p1.x + p2.x) / 2.0;
            double mid_y = (p1.y + p2.y) / 2.0;
            double a = 1.0 + k_verticle * k_verticle;
            double b = -2 * mid_x - k_verticle * k_verticle * (p1.x + p2.x);
            double c = mid_x * mid_x + k_verticle * k_verticle * (p1.x + p2.x) * (p1.x + p2.x) / 4.0 -
                    (dRadius * dRadius - ((mid_x - p1.x) * (mid_x - p1.x) + (mid_y - p1.y) * (mid_y - p1.y)));

            double val = sqrt(b * b - 4 * a * c);
            center1.x = (int) ((-1.0 * b + val) / (2 * a));
            center2.x = (int) ((-1.0 * b - val) / (2 * a));
            center1.y = (int) Y_Coordinates(mid_x, mid_y, k_verticle, center1.x);
            center2.y = (int) Y_Coordinates(mid_x, mid_y, k_verticle, center2.x);

            return new Point[]{center1, center2};
        } else {
            return new Point[]{new Point((p1.getX() + p2.getX()) / 2, (p1.getY() + p2.getY()) / 2)};
        }
    }

    static double Y_Coordinates(double x, double y, double k, double x0) {
        return k * x0 - k * x + y;
    }

    public static Point calLinePoint(Point ownerPoint, Point targetPoint, int range) {
        int distance = ownerPoint.distance(targetPoint);
        // 距离本来就比较近， 则不变更位置
        if (distance <= range) {
            return ownerPoint;
        }
        int startTo = ownerPoint.distance(targetPoint);
        int x = (ownerPoint.getX() - targetPoint.getX()) * range / startTo + targetPoint.getX();
        int y = targetPoint.getY() - (targetPoint.getY() - ownerPoint.getY()) * range / startTo;
        return new Point(x, y);
    }
}
