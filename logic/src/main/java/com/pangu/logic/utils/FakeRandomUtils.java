package  com.pangu.logic.utils;

/**
 * 伪随机生成器
 */
public class FakeRandomUtils {
    private static final int mask = (1 << 31) - 1;

    //产生一个为正数的伪随机数
    public static int random(int value) {
        value += (value << 15) ^ 0xffffcd7d;
        value ^= (value >>> 10);
        value += (value << 3);
        value ^= (value >>> 6);
        value += (value << 2) + (value << 14);
        value ^= (value >>> 16);
        return value & mask;
    }
}
