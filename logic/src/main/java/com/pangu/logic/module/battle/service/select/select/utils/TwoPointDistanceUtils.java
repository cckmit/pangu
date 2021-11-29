package com.pangu.logic.module.battle.service.select.select.utils;


import com.pangu.logic.module.battle.model.Point;

/**
 * <p>
 * 把矩形当成圆形
 * +----------+
 * |          |
 * |          |  radius
 * |     @----@------->* 客户端发送点
 * |          |圆形交界
 * |          |
 * +----------+
 * author weihongwei
 * date 2017/12/4
 */
public class TwoPointDistanceUtils {

    /**
     * 此类主要服务于法球效果
     * 即法球作用点超出施法距离，则将法球扔到法球作用半径距离圆形的交界点
     * 两点坐标，求距离起始点固定距离坐标
     *
     * @param start  圆形坐标
     * @param radius 圆形半径
     * @param end    圆形外一点
     * @return
     */
    public static Point getNearStartPoint(Point start, int radius, Point end) {
        int distance = start.distance(end);
        // 如果目标点在圆形内，则直接返回目标点
        if (radius >= distance) {
            return end;
        }
        // 以圆心为坐标原点，计算相对坐标
        int relativeX = end.getX() - start.getX();
        int relativeY = end.getY() - start.getY();

        double targetRadius = Math.sqrt(relativeX * relativeX + relativeY * relativeY);

        double crossX = (relativeX * radius) / targetRadius;
        if (crossX >= 0) {
            crossX += 0.5;
        } else {
            crossX -= 0.5;
        }
        relativeX = (int) crossX + start.getX();
        double crossY = (relativeY * radius) / targetRadius;
        if (crossY >= 0) {
            crossY += 0.5;
        } else {
            crossY -= 0.5;
        }
        relativeY = (int) crossY + start.getY();
        return new Point(relativeX, relativeY);
    }

    /**
     * 用于击退效果
     * 两点坐标，距离结束坐标点固定距离的坐标点，远离结束坐标
     *
     * @param start
     * @param end
     * @param range
     * @return
     */
    public static Point getNearEndPointDistance(Point start, Point end, int range) {
        int startTo = start.distance(end);
        if (startTo == 0) {
            return new Point(end.x, end.y);
        }
        int x = (end.x - start.x) * range / startTo + end.x;
        int y = (end.y - start.y) * range / startTo + end.y;
        return new Point(x, y);
    }
}
