package com.pangu.framework.utils.id;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 主键生成器<br>
 * [保留位:1][运营商:8][服务器位:13][服务器编号:6][主键自增位:36]
 * [保留][256][8192][64][687 1947 6736]
 */
@Slf4j
public class IdGenerator {

    static final int operatorBit = 8;
    static final int serverBit = 13;
    static final int versionBit = 6;
    static final int idxBit = 36;

    private static final long shortIdMax = (1L << idxBit) - 1;
    private static final int versionMax = (1 << versionBit) - 1;

    /**
     * 运营商标识
     */
    private final int operator;
    /**
     * 服务器标识
     */
    private final int server;

    private final int version;
    /**
     * 当前自增值
     */
    private final AtomicLong current;

    // 最小值
    private final long min;

    /**
     * 溢出边界
     */
    private final long limit;

    /**
     * 构造主键生成器
     *
     * @param operator 运营商标识
     * @param server   服务器标识
     * @param current  当前的主键值
     */
    public IdGenerator(int operator, int server, Long current) {
        this(operator, server, 0, current);
    }

    public IdGenerator(int operator, int server, int version, long current) {
        if (notValid(operator, operatorBit)) {
            throw new IllegalArgumentException("运营商标识[" + operator + "]超过了9位二进制数的表示范围");
        }
        if (notValid(server, serverBit)) {
            throw new IllegalArgumentException("服务器标识[" + server + "]超过了14位二进制数的表示范围");
        }
        this.operator = operator;
        this.server = server;
        this.version = version;

        final long[] limits = getLimits(operator, server);
        int shortId = toShortId(current);

        this.current = new AtomicLong(shortId);
        this.limit = limits[1];
        this.min = limits[0];
    }

    /**
     * 获取当前的主键值
     *
     * @return
     */
    public long getCurrent() {
        return current.get();
    }

    /**
     * 获取下一个主键值
     *
     * @return
     * @throws IllegalStateException 到达边界值时会抛出该异常
     */
    public long getNext() {
        long shortId = current.incrementAndGet();
        if (shortId > shortIdMax) {
            log.warn("主键值[" + shortId + "]已经超出了边界[" + limit + "]");
        }
        return min | (version & versionMax) | (shortId & shortIdMax);
    }

    // Getter and Setter ...

    public int getServer() {
        return server;
    }

    public int getOperator() {
        return operator;
    }

    public long getLimit() {
        return limit;
    }

    // Static Method's ...

    /**
     * 取主键的增长ID
     *
     * @param id
     * @return
     */
    public static int toShortId(long id) {
        // 将高位置0(保留位+运营商位+服务器位)
        return (int) (id & shortIdMax);
    }

    /**
     * 获取主键中的服标识
     *
     * @param id 主键值
     * @return
     */
    public static short toServer(long id) {
        // 将高位置0(保留位+运营商位+服务器位)
        return (short) ((id >> (versionBit + idxBit)) & ((1 << serverBit) - 1));
    }

    /**
     * 获取主键中的运营商标识
     *
     * @param id 主键值
     * @return
     */
    public static short toOperator(long id) {
        // 将高位置0(保留位+运营商位+服务器位)
        return (short) ((id >> (serverBit + versionBit + idxBit)) & ((1 << operatorBit) - 1));
    }

    /**
     * 通过id取逻辑服标识
     *
     * @param id 唯一标识
     * @return
     */
    public static String toOperatorServer(long id) {
        int operator = toOperator(id);
        int server = toServer(id);
        return operator + "_" + server;
    }

    /**
     * 检查值是否超过指定位数的2进制表示范围
     *
     * @param value 被检查的值
     * @param digit 2进制位数
     * @return true:合法,false:非法或超过范围
     */
    public static boolean notValid(int value, int digit) {
        if (digit <= 0 || digit > 64) {
            throw new IllegalArgumentException("位数必须在1-64之间");
        }
        int max = (1 << digit) - 1;
        return value < 0 || value > max;
    }

    /**
     * 获取ID值边界
     *
     * @param operator 运营商值
     * @param server   服务器值
     * @return [0]:最小值,[1]:最大值
     */
    public static long[] getLimits(int operator, int server) {
        if (notValid(operator, operatorBit)) {
            throw new IllegalArgumentException("运营商标识[" + operator + "]超过了" + operatorBit + "位二进制数的表示范围");
        }
        if (notValid(server, serverBit)) {
            throw new IllegalArgumentException("服务器标识[" + server + "]超过了" + serverBit + "位二进制数的表示范围");
        }

        long min = (((long) operator) << (serverBit + versionBit + idxBit)) | (((long) server) << (versionBit + idxBit));
        long max = min | ((1L << (versionBit + idxBit)) - 1);
        return new long[]{min, max};
    }

    /**
     * 获取ID值边界
     *
     * @param operator 运营商值
     * @return [0]:最小值,[1]:最大值
     */
    public static long[] getLimits(int operator) {
        if (notValid(operator, operatorBit)) {
            throw new IllegalArgumentException("运营商标识[" + operator + "]超过了12位二进制数的表示范围");
        }
        long min = (((long) operator) << (serverBit + versionBit + idxBit));
        long max = min | ((1L << (serverBit + versionBit + idxBit)) - 1);
        return new long[]{min, max};
    }

    /**
     * ID信息
     */
    public static class IdInfo {
        /**
         * 运营商标识
         */
        private final short operator;
        /**
         * 服务器标识
         */
        private final short server;
        /**
         * 标识
         */
        private final long id;

        /**
         * 自增部分
         */
        private final int increase;

        /**
         * 构造方法
         */
        public IdInfo(long id) {
            this.id = id;
            this.server = toServer(id);
            this.operator = toOperator(id);
            this.increase = toShortId(id);
        }

        // Getter and Setter ...

        /**
         * 获取服务器标识值
         *
         * @return
         */
        public short getServer() {
            return server;
        }

        /**
         * 获取运营商标识值
         *
         * @return
         */
        public short getOperator() {
            return operator;
        }

        /**
         * 获取去除运营商标识和服务器标识的ID值
         *
         * @return
         */
        public long getId() {
            return id;
        }

        public int getIncrease() {
            return increase;
        }
    }
}
