package com.pangu.logic.module.battle.service.utils;

public class Rect {

    public int minX;
    public int minY;

    public int maxX;
    public int maxY;
    private int x;
    private int y;
    private int width;

    private Rect() {
    }

    public Rect(int x, int y, int width) {
        this.x = x;
        this.y = y;
        this.width = width;
        int grid = width >> 1;
        minX = x - grid;
        minY = y - grid;
        maxX = x + grid;
        maxY = y + grid;
    }

    public boolean within(int cx, int cy) {
        return cx >= minX && cx <= maxX && cy >= minY && cy <= maxY;
    }

    public Rect cross(int cx, int cy) {
        if (!within(cx, cy)) {
            return null;
        }
        Rect rect = new Rect();
        if (cx >= x) {
            rect.minX = cx;
            rect.maxX = maxX;
            if (cy >= y) {
                rect.minY = cy;
                rect.maxY = maxY;
                return rect;
            } else {
                rect.minY = minY;
                rect.maxY = cy;
                return rect;
            }
        } else {
            rect.minX = minX;
            rect.maxX = cx;
            if (cy >= y) {
                rect.minY = cy;
                rect.maxY = maxY;
            } else {
                rect.minY = minY;
                rect.maxY = cy;
            }
            return rect;
        }
    }
}
