package com.pangu.framework.protocol.proxy;

import com.pangu.framework.protocol.exception.UnknowSignalException;
import com.pangu.framework.protocol.exception.WrongTypeException;
import com.pangu.framework.protocol.Context;
import com.pangu.framework.protocol.Types;
import io.netty.buffer.ByteBuf;

import java.lang.reflect.Array;

public class ArrayProxy extends AbstractProxy<Object> {

    @Override
    public Object getValue(Context ctx, byte flag) {
        ByteBuf buffer = ctx.getBuffer();
        byte type = getFlagTypes(flag);
        if (type != Types.ARRAY) {
            throw new WrongTypeException(Types.ARRAY, type);
        }

        byte signal = getFlagSignal(flag);
        if (signal == 0x00) {
            // #### 0000
            byte tag = buffer.readByte();
            int len = readVarInt32(buffer, tag);
            if (buffer.readableBytes() < len) {
                throw new IllegalStateException(buffer.readableBytes() + "小于" + len);
            }
            Object[] result = new Object[len];
            for (int i = 0; i < len; i++) {
                byte fValue = buffer.readByte();
                Object value = ctx.getValue(fValue);
                result[i] = value;
            }
            return result;
        }
        throw new UnknowSignalException(type, signal);
    }

    @Override
    public void setValue(Context ctx, Object value) {
        ByteBuf out = ctx.getBuffer();
        byte flag = Types.ARRAY;
        // #### 0000
        out.writeByte(flag);
        int len = Array.getLength(value);
        putVarInt32(out, len);
        for (int i = 0; i < len; i++) {
            Object obj = Array.get(value, i);
            ctx.setValue(obj);
        }
    }

}
