package com.pangu.logic.module.battle.service.utils;

public abstract class Utils {

    public static int squareIndex(int x, int y, int maxX) {
        return y * maxX + x;
    }
}
