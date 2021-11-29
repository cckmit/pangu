package com.pangu.logic.module.battle.service.select.select.utils;

import lombok.Getter;

/**
 * 圆形
 */
@Getter
public class Circle {
    private final int centerX;
    private final int centerY;
    private final int radiusSquare;
    private final int radius;

    public Circle(int x, int y, int radius) {
        this.centerX = x;
        this.centerY = y;
        this.radius = radius;
        this.radiusSquare = radius * radius;
    }

    public boolean inShape(int x, int y) {
        int len = (x - centerX) * (x - centerX) + (y - centerY) * (y - centerY);
        return len <= radiusSquare;
    }
}
