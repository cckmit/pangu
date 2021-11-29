package com.pangu.framework.protocol.def;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 枚举定义
 * @author author
 */
public class EnumDef implements Comparable<EnumDef> {

	private static final Logger log = LoggerFactory.getLogger(EnumDef.class);

	private int code;
	private Class<?> type;
	private String[] names;
	private Enum<?>[] values;

	public static EnumDef valueOf(int code, Class<?> type) {
		EnumDef e = new EnumDef();
		e.code = code;
		e.type = type;

		if (type == null) {
			return e;
		}

		@SuppressWarnings("unchecked")
		Class<? extends Enum<?>> enumClz = (Class<? extends Enum<?>>) type;
		Enum<?>[] values = enumClz.getEnumConstants();
		e.values = values;
		e.names = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			e.names[i] = values[i].name();
		}
		return e;
	}

	public static EnumDef valueOf(int code, String[] names) {
		EnumDef e = new EnumDef();
		e.code = code;
		e.names = names;
		return e;
	}

	public static EnumDef valueOf(ByteBuffer buf) throws IOException {
		// 类型, 类标识, (枚举类名长度, 枚举类名字节), 枚举数值数量, (名字长度, 名字字节)....
		short code = buf.getShort();
		short nLen = buf.getShort();
		byte[] nBytes = new byte[nLen];
		buf.get(nBytes);
		String clzName = new String(nBytes);

		int len = buf.getShort();
		String[] names = new String[len];
		for (int i = 0; i < len; i++) {
			int n = buf.getShort();
			byte[] a = new byte[n];
			buf.get(a);
			names[i] = new String(a);
		}

		Class<?> clz;
		try {
			clz = Class.forName(clzName);
			return EnumDef.valueOf(code, clz);
		} catch (ClassNotFoundException e) {
			// 类型不存在
			// throw new IOException(e);
			log.warn("枚举[{}]不存在, 当作String处理");
			clz = null;
		}
		return EnumDef.valueOf(code, names);
	}

	public void describe(ByteBuf buf) {
		// 类型, 类标识, (枚举类名长度, 枚举类名字节), 枚举数值数量, (名字长度, 名字字节)....
		int code = this.getCode();
		Class<?> enumClz = this.getType();
		String enumName = enumClz.getName();
		byte[] bytes = enumName.getBytes();
		buf.writeByte((byte) 0x00);
		buf.writeShort((short) code);
		buf.writeShort((short) bytes.length);
		buf.writeBytes(bytes);

		String[] names = this.getNames();
		buf.writeShort((short) values.length);
		for (String name : names) {
			byte[] nameBytes = name.getBytes();
			buf.writeShort((short) nameBytes.length);
			buf.writeBytes(nameBytes);
		}
	}

	public int getCode() {
		return code;
	}

	public Class<?> getType() {
		return type;
	}

	protected Enum<?>[] getValues() {
		return values;
	}

	public String[] getNames() {
		return names;
	}

	public Object getValue(int ordinal) {
		if (type != null) {
			return values[ordinal];
		}
		return names[ordinal];
	}

	@Override
	public int compareTo(EnumDef o) {
		return new CompareToBuilder().append(this.code, o.code).append(this.type, o.type).toComparison();
	}

}
