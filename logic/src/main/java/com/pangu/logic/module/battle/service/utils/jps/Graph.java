package com.pangu.logic.module.battle.service.utils.jps;


import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * @author Kevin
 */
public class Graph {

    public enum Diagonal {
        ALWAYS,
        NO_OBSTACLES,
        ONE_OBSTACLE,
        NEVER
    }

    private BitSet nodes;
    private int maxX;
    private int maxY;

    private Node[] cache;

    private BiFunction<Node, Node, Double> distance;
    private BiFunction<Node, Node, Double> heuristic;

    public Graph(int maxX, int maxY, DistanceAlgo distance, DistanceAlgo heuristic) {
        this.maxX = maxX;
        this.maxY = maxY;
        int amount = maxX * maxY;
        nodes = new BitSet(amount);
        cache = new Node[amount];
        for (int y = 0; y < maxY; ++y)
            for (int x = 0; x < maxX; ++x) {
                cache[y * maxX + x] = new Node(x, y);
            }

        this.distance = distance.algo;
        this.heuristic = heuristic.algo;
    }

    public Graph(int maxX, int maxY) {
        this(maxX, maxY, DistanceAlgo.EUCLIDEAN, DistanceAlgo.EUCLIDEAN);
    }

    public Node getNode(int x, int y) {
        if (x < 0 || x >= maxX || y < 0 || y >= maxY) {
            return null;
        }
        return cache[y * maxX + x];
    }

    /**
     * Given two adjacent nodes, returns the distance between them.
     *
     * @return The distance between the two nodes given.
     */
    public double getDistance(Node a, Node b) {
        return distance.apply(a, b);
    }

    /**
     * Given two nodes, returns the estimated distance between them. Optimizing this is the best way to improve
     * performance of your search time.
     *
     * @return Estimated distance between the two given nodes.
     */
    public double getHeuristicDistance(Node a, Node b) {
        return heuristic.apply(a, b);
    }

    /**
     * By default, we return all reachable diagonal neighbors that have no obstacles blocking us.
     * i.e.
     * O O G
     * O C X
     * O O O
     * <p>
     * In this above example, we could not go diagonally from our (C)urrent position to our (G)oal due to the obstacle (X).
     * <p>
     * Please use {@link #getNeighborsOf(Node, Diagonal)} method if you would like to specify different diagonal functionality.
     *
     * @return All reachable neighboring nodes of the given node.
     */
    public Collection<Node> getNeighborsOf(Node node) {
        return getNeighborsOf(node, Diagonal.NO_OBSTACLES);
    }

    /**
     * @return All reachable neighboring nodes of the given node.
     */
    public Set<Node> getNeighborsOf(Node node, Diagonal diagonal) {
        int x = node.x;
        int y = node.y;
        Set<Node> neighbors = new HashSet<>();

        boolean n = false, s = false, e = false, w = false, ne = false, nw = false, se = false, sw = false;

        // ?
        if (isWalkable(x, y - 1)) {
            neighbors.add(getNode(x, y - 1));
            n = true;
        }
        // ?
        if (isWalkable(x + 1, y)) {
            neighbors.add(getNode(x + 1, y));
            e = true;
        }
        // ?
        if (isWalkable(x, y + 1)) {
            neighbors.add(getNode(x, y + 1));
            s = true;
        }
        // ?
        if (isWalkable(x - 1, y)) {
            neighbors.add(getNode(x - 1, y));
            w = true;
        }

        switch (diagonal) {
            case NEVER:
                return neighbors;
            case NO_OBSTACLES:
                ne = n && e;
                nw = n && w;
                se = s && e;
                sw = s && w;
                break;
            case ONE_OBSTACLE:
                ne = n || e;
                nw = n || w;
                se = s || e;
                sw = s || w;
                break;
            case ALWAYS:
                ne = nw = se = sw = true;
        }

        // ?
        if (nw && isWalkable(x - 1, y - 1)) {
            neighbors.add(getNode(x - 1, y - 1));
        }
        // ?
        if (ne && isWalkable(x + 1, y - 1)) {
            neighbors.add(getNode(x + 1, y - 1));
        }
        // ?
        if (se && isWalkable(x + 1, y + 1)) {
            neighbors.add(getNode(x + 1, y + 1));
        }
        // ?
        if (sw && isWalkable(x - 1, y + 1)) {
            neighbors.add(getNode(x - 1, y + 1));
        }

        return neighbors;
    }

    public boolean isWalkable(int x, int y) {
        return x >= 0 && x < maxX && y >= 0 && y < maxY && !nodes.get(y * maxX + x);
    }

    public boolean exist(int x, int y) {
        return nodes.get(y * maxX + x);
    }

    public void set(int x, int y) {
        nodes.set(y * maxX + x);
    }

    public void reset() {
        nodes.clear();
    }

    /**
     * If you would like to define your own Distance Algorithm not included.
     */
    public void setDistanceAlgo(BiFunction<Node, Node, Double> distance) {
        this.distance = distance;
    }

    /**
     * If you would like to define your own Heuristic Algorithm not included.
     */
    public void setHeuristicAlgo(BiFunction<Node, Node, Double> heuristic) {
        this.heuristic = heuristic;
    }

    public enum DistanceAlgo {
        SINGLE_1(single_1),
        MANHATTAN(manhattan),
        EUCLIDEAN(euclidean),
        EUCLIDEAN_2(euclidean_pow),
        OCTILE(octile),
        CHEBYSHEV(chebyshev);

        BiFunction<Node, Node, Double> algo;

        DistanceAlgo(BiFunction<Node, Node, Double> algo) {
            this.algo = algo;
        }
    }

    private static BiFunction<Node, Node, Double> single_1 = (a, b) -> 1d;
    private static BiFunction<Node, Node, Double> manhattan = (a, b) -> (double) Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    private static BiFunction<Node, Node, Double> euclidean = (a, b) -> Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    private static BiFunction<Node, Node, Double> euclidean_pow = (a, b) -> Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2);
    private static BiFunction<Node, Node, Double> octile = (a, b) -> {
        double F = Math.sqrt(2) - 1;
        double dx = Math.abs(a.x - b.x);
        double dy = Math.abs(a.y - b.y);
        return (dx < dy) ? F * dx + dy : F * dy + dx;
    };
    private static BiFunction<Node, Node, Double> chebyshev = (a, b) -> (double) Math.max(Math.abs(a.x - b.x), Math.abs(a.y - b.y));
}
