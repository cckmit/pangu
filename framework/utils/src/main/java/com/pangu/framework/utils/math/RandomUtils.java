package com.pangu.framework.utils.math;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机数工具类
 * @author author
 */
public final class RandomUtils {

	/** 精确到小数点后10位 */
	public final static int RATE_BASE = 1000000000;

	private RandomUtils() {
		throw new IllegalAccessError("该类不允许实例化");
	}

	/**
	 * 检查是否命中
	 * @param rate 命中率
	 * @return 命中返回 true, 不命中返回 false
	 */
	public static boolean isHit(double rate) {
		return isHit(rate, ThreadLocalRandom.current());
	}

	/**
	 * 检查是否命中
	 * @param rate 命中率
	 * @param random 伪随机序列
	 * @return
	 */
	public static boolean isHit(double rate, Random random) {
		if (rate <= 0) {
			return false;
		}
		double value = random.nextDouble();
		return value <= rate;
	}

	/**
	 * 获取两个整数之间的随机数
	 * @param min 最小值
	 * @param max 最大值
	 * @param include 是否包含边界值
	 * @return
	 */
	public static int betweenInt(int min, int max, boolean include) {
		// 参数检查
		if (min > max) {
			throw new IllegalArgumentException("最小值[" + min + "]不能大于最大值[" + max + "]");
		} else if (!include && min == max) {
			throw new IllegalArgumentException("不包括边界值时最小值[" + min + "]不能等于最大值[" + max + "]");
		}
		// 修正边界值
		if (include) {
			max++;
		} else {
			min++;
		}
		return (int) (min + Math.random() * (max - min));
	}

	/**
	 * 获取两个整数之间的随机数,并返回为小数标识形式
	 * @param min 最小值
	 * @param max 最大值
	 * @param include 是否包含边界值
	 * @param scale 标度
	 * @return
	 */
	public static double betweenDouble(int min, int max, boolean include, int scale) {
		if (scale < 1) {
			throw new IllegalArgumentException("标度值不能小于1");
		}
		double random = betweenInt(min, max, include);
		return random / Math.pow(10, scale);
	}

	// 委托 Random 对象的方法

	/**
	 * @see Random#nextBoolean()
	 * @return
	 */
	public static boolean nextBoolean() {
		return ThreadLocalRandom.current().nextBoolean();
	}

	/**
	 * @see Random#nextBytes(byte[])
	 * @return
	 */
	public static void nextBytes(byte[] bytes) {
		ThreadLocalRandom.current().nextBytes(bytes);
	}

	/**
	 * @see Random#nextDouble()
	 * @return
	 */
	public static double nextDouble() {
		return ThreadLocalRandom.current().nextDouble();
	}

	/**
	 * @see Random#nextDouble()
	 * @return
	 */
	public static double nextDouble(double d) {
		return ThreadLocalRandom.current().nextDouble(d);
	}

	/**
	 * @see Random#nextFloat()
	 * @return
	 */
	public static float nextFloat() {
		return ThreadLocalRandom.current().nextFloat();
	}

	/**
	 * @see Random#nextInt()
	 * @return
	 */
	public static int nextInt() {
		return ThreadLocalRandom.current().nextInt();
	}

	/**
	 * @see Random#nextInt(int)
	 * @return
	 */
	public static int nextInt(int n) {
		return ThreadLocalRandom.current().nextInt(n);
	}

	/**
	 * @see Random#nextLong()
	 * @return
	 */
	public static long nextLong() {
		return ThreadLocalRandom.current().nextLong();
	}
}