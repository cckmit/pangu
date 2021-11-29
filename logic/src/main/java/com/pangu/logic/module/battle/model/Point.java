package com.pangu.logic.module.battle.model;

import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.framework.protocol.annotation.Transable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 坐标(以战场格子为相对)
 */
@Transable
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Point {

    // X 坐标
    public int x;

    // X 坐标
    public int y;

    public Point(Point point) {
        this.x = point.x;
        this.y = point.y;
    }


    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int distance(Point point) {
        return (int) Math.sqrt((point.x - x) * (point.x - x) + (point.y - y) * (point.y - y));
    }

    public void move(Point node) {
        this.x = node.x;
        this.y = node.y;
    }

    public void move(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean valid(){
        return x >= 0 && x <= BattleConstant.MAX_X && y >= 0 && y <= BattleConstant.MAX_Y;
    }
}
