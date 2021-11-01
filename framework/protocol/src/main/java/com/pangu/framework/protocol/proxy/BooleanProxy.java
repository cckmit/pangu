package com.pangu.framework.protocol.proxy;

import com.pangu.framework.protocol.exception.UnknowSignalException;
import com.pangu.framework.protocol.exception.WrongTypeException;
import io.netty.buffer.ByteBuf;

import java.util.concurrent.atomic.AtomicBoolean;

import com.pangu.framework.protocol.Context;
import com.pangu.framework.protocol.Types;

public class BooleanProxy extends AbstractProxy<Object> {

	@Override
	public Object getValue(Context ctx, byte flag) {
		// ByteBuffer in = ctx.getBuffer();
		byte type = getFlagTypes(flag);
		if (type != Types.BOOLEAN) {
			throw new WrongTypeException(Types.BOOLEAN, type);
		}

		byte signal = getFlagSignal(flag);
		if (signal == 0x00) {
			return false;
		} else if (signal == 0x01) {
			return true;
		}
		throw new UnknowSignalException(type, signal);
	}

	@Override
	public void setValue(Context ctx, Object value) {
		ByteBuf out = ctx.getBuffer();
		byte flag = Types.BOOLEAN;
		if (value instanceof Boolean) {
			Boolean bool = (Boolean) value;
			if (bool) {
				// #### 0001
				flag |= 0x01;
				out.writeByte(flag);
			} else {
				// #### 0000
				out.writeByte(flag);
			}
		} else if (value instanceof AtomicBoolean) {
			AtomicBoolean bool = (AtomicBoolean) value;
			if (bool.get()) {
				// #### 0001
				flag |= 0x01;
				out.writeByte(flag);
			} else {
				// #### 0000
				out.writeByte(flag);
			}
		}
	}

}
