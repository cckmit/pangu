package com.pangu.logic.module.battle.service.utils;

import com.pangu.logic.module.battle.model.Point;
import lombok.Getter;

import java.util.*;

/**
 * A*寻路算法，提高H值权重，意味寻路会优先往目标点移动
 */
@Getter
public class AStar {

    private BitSet nodes;
    private int xMax;
    private int yMax;

    public AStar(BitSet nodes, int xMax, int maxY) {
        init(nodes, xMax, maxY);
    }

    private void init(BitSet nodes, int xMax, int yMax) {
        this.nodes = nodes;
        this.xMax = xMax;
        this.yMax = yMax;
    }

    public AStar(int[][] nodes) {
        BitSet bitSet = new BitSet(nodes.length * nodes[0].length);
        int xMax = nodes[0].length;
        for (int y = 0; y < nodes.length; ++y) {
            for (int x = 0; x < nodes[y].length; ++x) {
                if (nodes[y][x] == 1) {
                    int index = y * xMax + x;
                    bitSet.set(index);
                }
            }
        }
        init(bitSet, xMax, nodes.length);
    }

    public List<Point> findPath(Point start, Point end, int withinDistance) {
        if (start.distance(end) <= withinDistance) {
            return Collections.emptyList();
        }
        Node startNode = new Node(start);
        Node endNode = new Node(end);
        PriorityQueue<Node> openList = new PriorityQueue<>();
        BitSet closeList = new BitSet(xMax * yMax);

        // 把起点加入 open list
        openList.add(startNode);
        // 循环次数控制
        int time = 0;
        Node result = null;
        while (((time++) <= 1000) && openList.size() > 0) {
            // 遍历 open list ，查找 F值最小的节点，把它作为当前要处理的节点
            Node currentNode = openList.poll();

            // 把这个节点移到 close list，closelist就是存储路径的链表
            closeList.set(currentNode.y * xMax + currentNode.x);

            // 查找不存在于close list当中的周围节点（不考虑斜边的邻居）
            ArrayList<Node> neighborNodes = findNeighborNodes(currentNode, closeList);

            // openlist其实就是存储的外围的节点集合
            for (Node node : neighborNodes) {// 总之要把邻居节点添加进openlist当中
                if (find(openList, node) != null) { // 如果邻居节点在openlist当中
                    foundPoint(currentNode, node);
                } else {
                    // 如果邻居节点不在openlist中，那就添加进openlist
                    notFoundPoint(currentNode, endNode, node);
                    openList.add(node);
                    if (node.distance <= withinDistance) {
                        result = node;
                        break;
                    }
                }
            }
            if (result != null) {
                break;
            }
            // 如果在openlist中找到了终点，那么就说明已经找到了路径，返回终点
            Node node = find(openList, endNode);
            if (node != null) {
                result = node;
            }
        }
        if (result == null) {
            if (openList.isEmpty()) {
                return Collections.emptyList();
            }
            result = findMinF(openList);
        }
        openList.clear();
        closeList.clear();

        return convertOrder(result);
    }

    void print(List<Node> openList, List<Node> closeList) {
        int size = nodes.size();
        int yMax = size / xMax;
        for (int y = 0; y < yMax; ++y) {
            for (int x = 0; x < xMax; ++x) {

                Node point = new Node(x, y);
                Node find;
                if ((find = find(openList, point)) != null) {
                    System.out.printf("%5d ", find.getF());
                    continue;
                } else if (find(closeList, point) != null) {
                    System.out.printf("%5s ", "#");
                    continue;
                }

                int index = y * xMax + x;
                boolean b = nodes.get(index);
                if (b) {
                    System.out.print(" B ");
                } else {
                    System.out.print(" _ ");
                }
            }
            System.out.println();
        }
        System.out.println();
        System.out.println();
    }

    private List<Point> convertOrder(Node node) {
        if (node == null || node.parent == null) {
            return Collections.emptyList();
        }
        List<Point> list = new ArrayList<>();
        do {
            list.add(new Point(node.getX(), node.getY()));
            node = node.parent;
        } while (node != null && node.parent != null);

        Collections.reverse(list);

        return list;
    }

    public static Node findMinF(Collection<Node> openList) {
        Node tempNode = null; // 先以第一个元素的F为最小值，然后遍历openlist的所有值，找出最小值
        for (Node node : openList) {
            if (tempNode == null) {
                tempNode = node;
                continue;
            }
            if (node.F < tempNode.F) {
                tempNode = node;
            }
        }
        return tempNode;
    }

    // 考虑周围节点的时候，就不把节点值为1的节点考虑在内，所以自然就直接避开了障碍物
    public ArrayList<Node> findNeighborNodes(Node currentNode, BitSet closeList) {
        ArrayList<Node> arrayList = new ArrayList<>();
        // 下
        int x = currentNode.x;
        int y = currentNode.y - 1;
        // canReach方法确保下标没有越界 exists方法确保此相邻节点不存在于closeList中，也就是之前没有遍历过
        if (canReach(x, y) && !closeList.get(x + y * xMax)) {
            arrayList.add(new Node(x, y));
        }
        // 上
        x = currentNode.x;
        y = currentNode.y + 1;
        if (canReach(x, y) && !closeList.get(x + y * xMax)) {
            arrayList.add(new Node(x, y));
        }

        // 左
        x = currentNode.x - 1;
        y = currentNode.y;
        if (canReach(x, y) && !closeList.get(x + y * xMax)) {
            arrayList.add(new Node(x, y));
        }
        // 右
        x = currentNode.x + 1;
        y = currentNode.y;
        if (canReach(x, y) && !closeList.get(x + y * xMax)) {
            arrayList.add(new Node(x, y));
        }
        return arrayList;
    }

    public boolean canReach(int x, int y) {
        if (x < 0 || y < 0 || x > xMax || y > yMax) {
            return false;
        }
        int index = y * xMax + x;
        return !nodes.get(index);
    }

    // 此种情况就是发现周围的F值最小的节点是之前已经遍历过了的，所以这个节点的G,H,F值都是已经计算过了的
    // 此时H值肯定不会变，所以要比较G值，如果现在的G值比之前的小，说明现在的路径更优
    // 接着就重置此节点的父指针，G值和F值
    private void foundPoint(Node tempStart, Node node) {
        int G = tempStart.G + 1;
        if (G < node.G) {
            node.parent = tempStart;
            node.G = G;
            node.calcF();
        }
    }

    // 这种情况是之前没有计算过此节点的值，所以在这里要计算一遍G,H,F值，然后确认父指针指向
    private void notFoundPoint(Node tempStart, Node end, Node node) {
        node.parent = tempStart;
        node.G = calcG(node);
        node.H = calcH(end, node);
        node.distance = (int) Math.sqrt(node.H);
        node.calcF();
    }

    private int calcG(Node node) {
        int parentG = node.parent != null ? node.parent.G : 0;
        return 1 + parentG;
    }

    // 使用三角函数，斜边的长度来作为H值，将距离近的优先级调为最高
    private int calcH(Node end, Node node) {
        int x = node.x - end.x;
        int y = node.y - end.y;
        return x * x + y * y;
    }

    public static Node find(Collection<Node> nodes, Node point) {
        for (Node n : nodes)
            if ((n.x == point.x) && (n.y == point.y)) {
                return n;
            }
        return null;
    }

    public static boolean notFound(List<Node> nodes, int x, int y) {
        for (Node n : nodes) {
            if ((n.x == x) && (n.y == y)) {
                return false;
            }
        }
        return true;
    }
}