package com.pangu.logic.module.battle.service.utils;

import com.pangu.logic.module.battle.model.Point;
import lombok.Getter;

@Getter
public class Node implements Comparable<Node> {
    public Node(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Node(Point position) {
        this.x = position.getX();
        this.y = position.getY();
    }

    public int x;
    public int y;

    public int F;
    public int G;
    public int H;

    public int distance;

    public void calcF() {
        this.F = this.G + this.H;
    }

    // 寻路时使用(反序)
    public Node parent;

    // 寻路结束后使用(顺序)
    public Node next;

    @Override
    public int compareTo(Node o) {
        return Integer.compare(F, o.F);
    }
}