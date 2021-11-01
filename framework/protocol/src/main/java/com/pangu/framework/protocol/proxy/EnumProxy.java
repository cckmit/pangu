package com.pangu.framework.protocol.proxy;

import com.pangu.framework.protocol.exception.UnknowEnumDefException;
import com.pangu.framework.protocol.exception.WrongTypeException;
import com.pangu.framework.protocol.Context;
import com.pangu.framework.protocol.Types;
import com.pangu.framework.protocol.def.EnumDef;
import io.netty.buffer.ByteBuf;

public class EnumProxy extends AbstractProxy<Object> {

	@Override
	public Object getValue(Context ctx, byte flag) {
		ByteBuf buffer = ctx.getBuffer();
		byte type = getFlagTypes(flag);
		if (type != Types.ENUM) {
			throw new WrongTypeException(Types.ENUM, type);
		}

		// byte signal = getFlagSignal(flag);
		// if (signal == 0x00) {
		// #### 0000
		// 枚举类型
		byte tagType = buffer.readByte();
		int enumType = readVarInt32(buffer, tagType);
		EnumDef def = ctx.getEnumDef(enumType);
		if (def == null) {
			if (log.isWarnEnabled()) {
				log.warn("枚举定义[{}]不存在", enumType);
			}
			throw new UnknowEnumDefException(enumType);
		}
		// 枚举值
		byte tagValue = buffer.readByte();
		int ordinal = readVarInt32(buffer, tagValue);
		return def.getValue(ordinal);
	}

	@Override
	public void setValue(Context ctx, Object obj) {
		ByteBuf out = ctx.getBuffer();
		byte flag = Types.ENUM;
		
		if(!(obj instanceof Enum)) {
			if (log.isWarnEnabled()) {
				log.warn("对象[{}]不是枚举", obj);
			}
			return;
		}
		
		Enum<?> value = (Enum<?>) obj;
		@SuppressWarnings("unchecked")
		Class<? extends Enum<?>> fclz = (Class<? extends Enum<?>>) value.getClass();
		EnumDef def = ctx.getEnumDef(fclz);
		if(def == null) {
			// throw new UnknowTypeDefException(fclz);
			if (log.isWarnEnabled()) {
				log.warn("枚举定义[{}]不存在", fclz);
			}
			String name = value.name();
			ctx.setValue(name);
			return;
		}
		
		// #### 0000
		out.writeByte(flag);
		
		int code = def.getCode();
		int ordinal = value.ordinal();
		putVarInt32(out, code);
		putVarInt32(out, ordinal);
	}

}
