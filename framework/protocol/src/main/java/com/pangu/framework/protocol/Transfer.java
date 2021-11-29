package com.pangu.framework.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 编解码上下文
 * @author author
 */
public class Transfer {
	protected Logger log = LoggerFactory.getLogger(getClass());

	private final Definition definition;

	public void register(Class<?> clz, int index) {
		if (log.isDebugEnabled()) {
			log.debug("注册传输对象类型 [{}]", clz);
		}
		definition.register(clz, index);
	}

	/**
	 * 获取消息定义
	 */
	public byte[] getDescription() {
		return definition.getDescription();
	}

	/**
	 * 获取消息定义
	 */
	public void setDescribe(byte[] bytes) throws IOException {
		definition.setDescribe(bytes);
	}

	/**
	 * 获取消息定义MD5串
	 */
	public String getMD5Description() {
		return definition.getDescriptionMD5();
	}

	/**
	 * 对象编码
	 * @param value
	 * @return
	 */
	public ByteBuf encode(ByteBufAllocator alloc, Object value) {
		ByteBuf buf = alloc.buffer();
		Context ctx = build(buf);
		ctx.setValue(value);
		return buf;
	}

	/**
	 * 对象解码
	 * @param buf
	 * @return
	 * @throws IOException
	 */
	public Object decode(ByteBuf buf){
		Context ctx = build(buf);
		if (!buf.isReadable()) {
			// return null;
			throw new IllegalArgumentException("Empty ByteBuffer...");
		}
		byte flag = buf.readByte();
		return ctx.getValue(flag);
	}

	/**
	 * 对象解码
	 * @param buf
	 * @param type
	 * @return
	 * @throws IOException
	 */
	public <T> T decode(ByteBuf buf, Type type) throws IOException {
		if (!buf.isReadable()) {
			// return null;
			throw new EOFException("Empty ByteBuffer...");
		}
		Context ctx = build(buf);
		Object v = ctx.start(type);
		return convert(v, type);
	}

	/**
	 * 对象类型转换
	 * @param value
	 * @param type
	 * @return
	 */
	public <T> T convert(Object value, Type type) {
		return definition.convert(value, type);
	}

	// ----------

	private Context build(ByteBuf buffer) {
		return new Context(buffer, definition);
	}

	// ----------

	public Transfer() {
		this.definition = new Definition();
	}

	public Transfer(byte[] description) throws IOException {
		// 类型注册
		this.definition = new Definition(description);
	}

	public Transfer(Collection<Class<?>> clzs, int startIndex) {
		// 类型注册
		this.definition = new Definition(clzs, startIndex);
	}

}
