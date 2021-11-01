package com.pangu.framework.utils.id;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 主键生成器<br>
 * [保留位:1][运营商:9][服务器位:14][系统到达现在有多少个半个小时:21][主键自增位:19]
 * 为确保尽可能保证生成的ID的唯一性，防止服务器清库之后，生成重复的玩家ID或者订单ID，时间单位保留21位
 * BASE_UNIT = 2 * 60 * 1000 精确度位2分钟，即两分钟之内支持id个数位 2<<19 - 1 	为52_4287个
 * 时间阶段支持为 2020-01-01到2027-01-01
 * 所以当日期超过2027年时，本系统已经不支持
 */
@Slf4j
public class IdGenerator {

    private static final int operatorBit = 9;
    private static final int serverBit = 14;

    private static final int shortIdMax = (1 << 19) - 1;
    private static final int dayHourMax = (1 << 21) - 1;
    public static final int BASE_UNIT = 2 * 60 * 1000;
    public static final long S_2020_01_01 = 1577808000000L;
    public static final long S_2027_01_01 = 1798732800000L;

    /**
     * 运营商标识
     */
    private final int operator;
    /**
     * 服务器标识
     */
    private final int server;
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
        if (!vaildValue(operator, operatorBit)) {
            throw new IllegalArgumentException("运营商标识[" + operator + "]超过了9位二进制数的表示范围");
        }
        if (!vaildValue(server, serverBit)) {
            throw new IllegalArgumentException("服务器标识[" + server + "]超过了14位二进制数的表示范围");
        }
        this.operator = operator;
        this.server = server;

        final long[] limits = getLimits(operator, server);
        if (current != null) {
            if (current < limits[0] || current > limits[1]) {
                throw new IllegalArgumentException("当前主键值[" + current + "],不符合运营商标识[" + operator + "]服务器标识[" + server
                        + "]的要求");
            }
            int shortId = toShortId(current);

            this.current = new AtomicLong(shortId);
        } else {
            this.current = new AtomicLong(0);
        }
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
        long day = getHalfHourCount();
        long shortId = current.incrementAndGet();

        long result = min | ((day & 0x1F_FF_FF) << 19) | (shortId & shortIdMax);

        if (result > limit) {
            throw new IllegalStateException("主键值[" + result + "]已经超出了边界[" + limit + "]");
        }
        return result;
    }

    private long getHalfHourCount() {
        long cur = System.currentTimeMillis();
        if (cur > S_2027_01_01) {
            log.warn("唯一id支持2020-01-01到2027-01-01，当前时间已经超过区间，请修改id生成器");
        }
        return (cur - S_2020_01_01) / BASE_UNIT;
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
        if ((0x80_00_00_00_00_00_00_00L & id) != 0) {
            throw new IllegalArgumentException("无效的ID标识值:" + id);
        }
        // 将高位置0(保留位+运营商位+服务器位)
        return (int) (id & shortIdMax);
    }

    /**
     * 取主键的天数标志
     *
     * @param id
     * @return
     */
    public static int toDay(long id) {
        if ((0x80_00_00_00_00_00_00_00L & id) != 0) {
            throw new IllegalArgumentException("无效的ID标识值:" + id);
        }
        // 将高位置0(保留位+运营商位+服务器位)
        return (int) ((id >> 19) & dayHourMax);
    }

    /**
     * 获取主键中的服标识
     *
     * @param id 主键值
     * @return
     */
    public static short toServer(long id) {
        if ((0x80_00_00_00_00_00_00_00L & id) != 0) {
            throw new IllegalArgumentException("无效的ID标识值:" + id);
        }
        // 将高位置0(保留位+运营商位+服务器位)
        return (short) ((id >> 40) & 0x00_00_00_00_00_00_3F_FFL);
    }

    /**
     * 获取主键中的运营商标识
     *
     * @param id 主键值
     * @return
     */
    public static short toOperator(long id) {
        if ((0x80_00_00_00_00_00_00_00L & id) != 0) {
            throw new IllegalArgumentException("无效的ID标识值:" + id);
        }
        // 将高位置0(保留位+运营商位+服务器位)
        return (short) ((id >> 54) & 0x00_00_00_00_00_00_01_FFL);
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
    public static boolean vaildValue(int value, int digit) {
        if (digit <= 0 || digit > 64) {
            throw new IllegalArgumentException("位数必须在1-64之间");
        }
        int max = (1 << digit) - 1;
        if (value >= 0 && value <= max) {
            return true;
        }
        return false;
    }

    /**
     * 获取ID值边界
     *
     * @param operator 运营商值
     * @param server   服务器值
     * @return [0]:最小值,[1]:最大值
     */
    public static long[] getLimits(int operator, int server) {
        if (!vaildValue(operator, operatorBit)) {
            throw new IllegalArgumentException("运营商标识[" + operator + "]超过了9位二进制数的表示范围");
        }
        if (!vaildValue(server, serverBit)) {
            throw new IllegalArgumentException("服务器标识[" + server + "]超过了14位二进制数的表示范围");
        }

        long min = (((long) operator) << 54) + (((long) server) << 40);
        long max = min | 0x00_00_00_FF_FF_FF_FF_FFL;
        return new long[]{min, max};
    }

    /**
     * 获取ID值边界
     *
     * @param operator 运营商值
     * @return [0]:最小值,[1]:最大值
     */
    public static long[] getLimits(int operator) {
        if (!vaildValue(operator, 10)) {
            throw new IllegalArgumentException("运营商标识[" + operator + "]超过了12位二进制数的表示范围");
        }
        long min = (((long) operator) << 54);
        long max = min | 0x00_3F_FF_FF_FF_FF_FF_FFL;
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
            this.id = id & 0x00_00_00_FF_FF_FF_FF_FFL; // 将高位置0(保留位+运营商位+服务器位)
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
    }
}
