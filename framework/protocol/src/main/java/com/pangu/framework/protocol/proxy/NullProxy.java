package com.pangu.framework.protocol.proxy;

import com.pangu.framework.protocol.Context;
import com.pangu.framework.protocol.Types;
import io.netty.buffer.ByteBuf;

public class NullProxy extends AbstractProxy<Object> {

	@Override
	public Object getValue(Context ctx, byte flag) {
		// 0000 0001 (1 - 0x01)
		return null;
	}

	@Override
	public void setValue(Context ctx, Object value) {
		ByteBuf out = ctx.getBuffer();
		byte flag = Types.NULL;
		out.writeByte(flag);
	}
}
