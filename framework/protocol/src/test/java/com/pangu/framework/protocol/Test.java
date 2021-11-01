package com.pangu.framework.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.pangu.framework.utils.model.Result;

import static org.junit.Assert.*;

public class Test {

	ByteBufAllocator alloc = new PooledByteBufAllocator();

	@org.junit.Test
	public void test() throws Exception {
		ByteBuf buf = alloc.buffer(100);
		List<Class<?>> list = new ArrayList<Class<?>>();
		list.add(Person.class);
		// list.add(Pet.class);
		list.add(Status.class);
		list.add(Result.class);
		Transfer enc = new Transfer(list, 0);
		{
			buf.writeInt(1);
			byte[] dst = new byte[4];
			buf.readBytes(dst);
			assertArrayEquals(new byte[] { 0, 0, 0, 1 }, dst);
			buf.resetReaderIndex();
		}

		Transfer dec = new Transfer(enc.getDescription());
		{
			ByteBuf r1 = enc.encode(alloc, Integer.MAX_VALUE);
			Object r = dec.decode(r1);
			assertEquals(r, Integer.MAX_VALUE);
		}
		
		{
			Result<Person> r1 = Result.SUCCESS(Person.valueOf(1, "NAME"));
			buf = enc.encode(alloc, r1);
			@SuppressWarnings("unchecked")
			Result<Person> r2 = (Result<Person>) dec.decode(buf);
			assertEquals(r1.getContent(), r2.getContent());
		}

		{
			int[] array = new int[1];
			array[0] = 1;
			buf = enc.encode(alloc, array);
			int[] r1 = dec.decode(buf, array.getClass());
			assertArrayEquals(array, r1);

		}
		for (int i = 0; i < Integer.MAX_VALUE; i = (i + 1) * 1000) {
			buf = enc.encode(alloc, i);
			Object r1 = dec.decode(buf);
			assertEquals(i, r1);
			if (i < 0) {
				break;
			}
		}
		for (int i = 0; i < Integer.MAX_VALUE; i = (i + 1) * 1000) {
			buf = enc.encode(alloc, -i);
			Object r1 = dec.decode(buf);
			assertEquals(-i, r1);
			if (i < 0) {
				break;
			}
		}
		for (long i = 0; i < 0x00FFFFFFFFFFFFFFL; i = (i + 1) * 1000) {
			buf = enc.encode(alloc, i);
			Object r1 = dec.decode(buf);
			if (r1 instanceof String) {
				r1 = Long.parseLong(r1.toString());
			}
			assertEquals(i, r1);
			if (i < 0) {
				break;
			}
		}
		for (long i = 0; i < 0x00FFFFFFFFFFFFFFL; i = (i + 1) * 1000) {
			buf = enc.encode(alloc, -i);
			Object r1 = dec.decode(buf);
			if (r1 instanceof String) {
				r1 = Long.parseLong(r1.toString());
			}
			assertEquals(-i, r1);
			if (i < 0) {
				break;
			}
		}

		{
			Object[] objs = new Object[] { false, true, 100, "Hello", "Hello", "Hello", "Hello" };
			Object[] arr = new Object[] { objs, objs, objs, objs };
			buf = enc.encode(alloc, arr);
			Object[] r1 = (Object[]) dec.decode(buf);
			assertArrayEquals(arr, r1);
			System.out.println(buf.writerIndex());
		}

		{
			Person person = Person.valueOf(1, "OK");
			Person person2 = Person.valueOf(1, "OK");
			List<Person> ps = new ArrayList<Person>();
			ps.add(person2);
			person.setList(ps);
			buf = enc.encode(alloc, person);
			Object r1 = dec.decode(buf);
			assertEquals(person, r1);
		}

		{
			Person person = Person.valueOf(1, "FAL");
			buf = enc.encode(alloc, person);
			person.setOk("NO");
			Object r1 = dec.decode(buf);
			assertNotEquals(person, r1);
		}

		{
			Person person = Person.valueOf(2, "N2");
			Object[] arr = new Object[] { person, person, person, person, person, person, person };
			buf = enc.encode(alloc, arr);
			Object[] r1 = (Object[]) dec.decode(buf);
			assertArrayEquals(arr, r1);
			System.out.println(buf.writerIndex());
		}

		{
			Map<String, Object> person = new TreeMap<String, Object>();
			person.put("name", "HELLO");
			person.put("id", 1L);
			person.put("age", 20);
			buf = enc.encode(alloc, person);
			Object r1 = dec.decode(buf);
			assertEquals(person, r1);
		}

		{
			Map<String, Object> m1 = new HashMap<String, Object>();
			Map<String, Object> m2 = new HashMap<String, Object>();
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("1", m1);
			map.put("2", m2);

			buf = enc.encode(alloc, map);
			@SuppressWarnings("unchecked")
			Map<String, Object> r1 = (Map<String, Object>) dec.decode(buf);
			assertNotSame(r1.get("1"), r1.get("2"));
		}

	}
}
