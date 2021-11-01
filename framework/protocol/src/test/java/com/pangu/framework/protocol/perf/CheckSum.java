package com.pangu.framework.protocol.perf;

import java.lang.reflect.Method;

import com.pangu.framework.protocol.Transfer;
import com.pangu.framework.utils.codec.HashUtils;

public class CheckSum {

	@org.junit.Test
	public void test() throws Exception {
		Transfer enc = new Transfer();
		byte[] bytes = enc.getDescription();
		int len = bytes.length;

		int total = 1000000;

		Method[] ms = HashUtils.class.getDeclaredMethods();
		for (Method m : ms) {
			String name = m.getName();
			Class<?>[] ps = m.getParameterTypes();
			if (ps.length == 2) {
				long s = System.currentTimeMillis();
				for (int i = 0; i < total; i++) {
					m.invoke(HashUtils.class, bytes, len);
				}
				System.out.println(name + ",\t 长度 -" + len + ",\t HASH" + total
						+ "次总时间" + (System.currentTimeMillis() - s));
			}
		}
	}

}
