package com.pangu.framework.utils.id;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于本地时间的伪唯一标识生成器
 * @author Ramon
 */
public class PseudoIdGenerator {

	private final AtomicInteger sequence = new AtomicInteger();
	private final Random random = new Random();

	/**
	 * 获取随机的唯一标识<br>
	 * [32][24][4][4]<br>
	 * [当前时间的秒数][自增值][随机值][检验码]<br>
	 * @return 唯一标识
	 */
	public long nextId() {
		long prefix = (System.currentTimeMillis() / 1000) % 0xFFFFFFFFL;
		long sn = sequence.getAndIncrement() % 0x00FFFFFFL;
		long rand = random.nextInt(0x0F);
		long result = (prefix << 32) | (sn << 8) | (rand << 4);
		byte[] b1 = longToByte(result, new byte[8], 0);
		long suffix = 0;
		for (byte b : b1) {
			suffix += (b & 0xFF);
		}
		suffix = suffix % 0x0FL;
		result |= suffix;
		return result;
	}

	public final static byte[] longToByte(long number, byte[] array, int offset) {
		array[offset + 7] = (byte) number;
		array[offset + 6] = (byte) (number >> 8);
		array[offset + 5] = (byte) (number >> 16);
		array[offset + 4] = (byte) (number >> 24);
		array[offset + 3] = (byte) (number >> 32);
		array[offset + 2] = (byte) (number >> 40);
		array[offset + 1] = (byte) (number >> 48);
		array[offset] = (byte) (number >> 56);
		return array;
	}

}
