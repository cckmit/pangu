package com.pangu.framework.protocol.perf;

import com.pangu.framework.protocol.Transfer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.pangu.framework.protocol.Person;
import com.pangu.framework.protocol.Pet;
import com.pangu.framework.protocol.Status;

public class PerfTest {
	ByteBufAllocator alloc = new PooledByteBufAllocator();

	@org.junit.Test
	public void test() throws Exception {
		List<Class<?>> list = new ArrayList<Class<?>>();
		list.add(Person.class);
		list.add(Pet.class);
		list.add(Status.class);

		Transfer enc = new Transfer(list, 0);
		byte[] description = enc.getDescription();
		Transfer dec = new Transfer(description);
		ByteBuf next = null;
		int total = 100000;

		// 基础类型
		{
			System.out.println("字符串:");
			{
				long s = System.currentTimeMillis();
				for (int i = 0; i < total; i++) {
					String obj = "Hello Hello1 Hello1 Hello2";
					ByteBuf buf = enc.encode(alloc, obj);
					if (next == null) {
						next = buf;
					} else {
						buf.release();
					}

				}
				System.out.println(total + "次编码时间" + (System.currentTimeMillis() - s));
			}
			{
				long s = System.currentTimeMillis();
				for (int i = 0; i < total; i++) {
					next.resetReaderIndex();
					// Object r1 =
					dec.decode(next);
					// assertArrayEquals(arr, r1);
				}
				System.out.println(total + "次解码时间" + (System.currentTimeMillis() - s));
			}
			int len = next.writerIndex();
			System.out.println("编码后大小 " + len + "------------------------\n");
			next = null;
		}

		// 基础数组
		{
			System.out.println("字符串数组:");
			{
				long s = System.currentTimeMillis();
				for (int i = 0; i < total; i++) {
					Object obj = new String[] { "Hello", "Hello1", "Hello1", "Hello2" };
					ByteBuf buf = enc.encode(alloc, obj);

					if (next == null) {
						next = buf;
					} else {
						buf.release();
					}

				}
				System.out.println(total + "次编码时间" + (System.currentTimeMillis() - s));
			}
			{
				long s = System.currentTimeMillis();
				for (int i = 0; i < total; i++) {
					next.resetReaderIndex();
					// Object r1 =
					dec.decode(next);
					// assertArrayEquals(arr, r1);
				}
				System.out.println(total + "次解码时间" + (System.currentTimeMillis() - s));
			}
			int len = next.writerIndex();
			System.out.println("编码后大小 " + len + "------------------------\n");
			next = null;
		}

		// 简单对象
		{
			System.out.println("对象:");
			{
				long s = System.currentTimeMillis();
				for (int i = 0; i < total; i++) {
					Object obj = Person.valueOf(i, "Hello1");
					ByteBuf buf = enc.encode(alloc, obj);

					if (next == null) {
						next = buf;
					} else {
						buf.release();
					}

				}
				System.out.println(total + "次编码时间" + (System.currentTimeMillis() - s));
			}
			{
				long s = System.currentTimeMillis();
				for (int i = 0; i < total; i++) {
					next.resetReaderIndex();
					// Object r1 =
					dec.decode(next);
					// assertArrayEquals(arr, r1);
				}
				System.out.println(total + "次解码时间" + (System.currentTimeMillis() - s));
			}
			int len = next.writerIndex();
			System.out.println("编码后大小 " + len + "------------------------\n");
			next = null;
		}

		// 简单对象数组
		{
			System.out.println("对象数组:");
			{
				long s = System.currentTimeMillis();
				for (int i = 0; i < total; i++) {
					Object obj = new Object[] { Person.valueOf(i, "Hello1"), Person.valueOf(i + 1, "Hello1"),
						Person.valueOf(i + 1, "Hello1") };
					ByteBuf buf = enc.encode(alloc, obj);

					if (next == null) {
						next = buf;
					} else {
						buf.release();
					}

				}
				System.out.println(total + "次编码时间" + (System.currentTimeMillis() - s));
			}
			{
				long s = System.currentTimeMillis();
				for (int i = 0; i < total; i++) {
					next.resetReaderIndex();
					// Object r1 =
					dec.decode(next);
					// assertArrayEquals(arr, r1);
				}
				System.out.println(total + "次解码时间" + (System.currentTimeMillis() - s));
			}
			int len = next.writerIndex();
			System.out.println("编码后大小 " + len + "------------------------\n");
			next = null;
		}

		// 超复杂数组
		{
			{
				System.out.println("超复杂对象数组:");
				long s = System.currentTimeMillis();
				for (int i = 0; i < total; i++) {
					Object[] objs = new Object[] { false, true, 100, -100, 100000L, "Hello1", "Hello2", "Hello1",
						"Hello2" };
					Person person = Person.valueOf(1, "Hello1");
					Map<String, Object> map = new TreeMap<String, Object>();
					map.put("name", "Hello2");
					map.put("id", 1L);
					map.put("age", 20);
					Object[] arr = new Object[] { person, map, objs, new byte[] { 0, 0, 0, 1 } };

					// Object arr = Person.valueOf(1, "Hello1");
					ByteBuf buf = enc.encode(alloc, arr);

					if (next == null) {
						next = buf;
					} else {
						buf.release();
					}

				}
				System.out.println(total + "次编码时间" + (System.currentTimeMillis() - s));
			}

			{
				long s = System.currentTimeMillis();
				for (int i = 0; i < total; i++) {
					next.resetReaderIndex();
					// Object r1 =
					dec.decode(next);
					// assertArrayEquals(arr, r1);
				}
				System.out.println(total + "次解码时间" + (System.currentTimeMillis() - s));
			}
			int len = next.writerIndex();
			System.out.println("编码后大小 " + len + "------------------------\n");
			next = null;
		}
	}
}
