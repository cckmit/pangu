package com.pangu.logic.module.battle.service.select.select.utils;

import com.pangu.logic.module.battle.model.Point;

/**
 * 扇形目标
 */
public class FanShaped {

    private static final int PROBABLE = 5;

    private final int startX;
    private final int startY;
    private final int targetX;
    private final int targetY;
    private final double degree;
    private final int maxLenSquare;
    private final double startToTargetLength;

    public FanShaped(Point start, Point target, int degree, int length) {
        this.startX = start.getX();
        this.startY = start.getY();
        this.targetX = target.getX();
        this.targetY = target.getY();
        this.degree = Math.toRadians(degree + 4) / 2;
        this.maxLenSquare = (length + PROBABLE) * (length + PROBABLE);

        startToTargetLength = Math.sqrt((targetX - startX) * (targetX - startX) + (targetY - startY) * (targetY - startY));
    }

    public boolean inShape(int x, int y) {
        // 距离判断
        int length = (x - startX) * (x - startX) + (y - startY) * (y - startY);
        if (length > maxLenSquare) {
            return false;
        }
        if (length == 0) {
            return true;
        }
        // 与中线角度 A.B = |a| * |b| * cos$
        int aDotB = (targetX - startX) * (x - startX) + (targetY - startY) * (y - startY);
        double acos = Math.acos(aDotB
                /
                (startToTargetLength * Math.sqrt(length)));
        return acos <= degree;
    }
}
