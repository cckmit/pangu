package com.pangu.framework.protocol;

import com.pangu.framework.protocol.def.EnumDef;
import com.pangu.framework.protocol.def.TypeDef;
import com.pangu.framework.protocol.exception.ManagedResultException;
import com.pangu.framework.protocol.exception.WrongTypeException;
import com.pangu.framework.protocol.proxy.AbstractProxy;
import com.pangu.framework.protocol.proxy.Proxy;
import io.netty.buffer.ByteBuf;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.reflect.TypeUtils;

import com.pangu.framework.utils.model.Result;

/**
 * 编解码上下文
 * @author author
 */
public class Context {
	private final ByteBuf buffer;
	private final Definition root;

	Context(ByteBuf buffer, Definition root) {
		this.buffer = buffer;
		this.root = root;
	}

	public ByteBuf getBuffer() {
		return buffer;
	}

	public TypeDef getTypeDef(int def) {
		return root.getTypeDef(def);
	}

	public TypeDef getTypeDef(Class<?> def) {
		return root.getTypeDef(def);
	}

	public EnumDef getEnumDef(int def) {
		return root.getEnumDef(def);
	}

	public EnumDef getEnumDef(Class<? extends Enum<?>> def) {
		return root.getEnumDef(def);
	}

	public TypeDef getMappedDef(Class<?> type) {
		return root.getMappedDef(type, true);
	}

	public Object getValue(byte flag) {
		byte type = AbstractProxy.getFlagTypes(flag);
		Proxy<?> proxy = root.getProxy(type);
		if (proxy != null) {
			return proxy.getValue(this, flag);
		}
		throw new WrongTypeException(type);
	}

	@SuppressWarnings("unchecked")
	public <T> T start(Type clz) {
		byte flag = buffer.readByte();
		Object value = this.getValue(flag);
		if (value instanceof Result) {
			if (TypeUtils.isAssignable(clz, Result.class)) {
				T t = (T) value;
				return t;
			}
			Result<?> r = (Result<?>) value;
			throw new ManagedResultException(r.getCode(), r);
		}
		T t = (T) value;
		return t;
	}

	public <T> T getValue(byte flag, Type clz) {
		Object value = this.getValue(flag);
		return root.convert(value, clz);
	}

	public void setValue(Object value) {
		byte type;
		// 类型
		if (value == null) {
			type = Types.NULL;
		} else if (value instanceof Number) {
			type = Types.NUMBER;
		} else if (value instanceof String) {
			type = Types.STRING;
		} else if (value instanceof Boolean || value instanceof AtomicBoolean) {
			type = Types.BOOLEAN;
		} else if (value instanceof Enum<?>) {
			type = Types.ENUM;
		} else if (value instanceof Date) {
			type = Types.DATE_TIME;
		} else if (value instanceof Map) {
			type = Types.MAP;
		} else if (value instanceof byte[]) {
			type = Types.BYTE_ARRAY;
		} else if (value.getClass().isArray()) {
			type = Types.ARRAY;
		} else if (value instanceof Collection) {
			type = Types.COLLECTION;
		} else {
			type = Types.OBJECT;
		}

		@SuppressWarnings("unchecked")
		Proxy<Object> proxy = (Proxy<Object>) root.getProxy(type);
		if (proxy != null) {
			proxy.setValue(this, value);
		} else {
			throw new WrongTypeException(type);
		}
	}
}
