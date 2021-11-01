package com.pangu.framework.protocol.proxy;

import com.pangu.framework.protocol.exception.WrongTypeException;
import io.netty.buffer.ByteBuf;

import com.pangu.framework.protocol.Context;
import com.pangu.framework.protocol.Types;

public class BytesProxy extends AbstractProxy<byte[]> {

    @Override
    public byte[] getValue(Context ctx, byte flag) {
        ByteBuf buffer = ctx.getBuffer();
        byte type = getFlagTypes(flag);
        if (type != Types.BYTE_ARRAY) {
            throw new WrongTypeException(Types.BYTE_ARRAY, type);
        }

        // byte signal = getFlagSignal(flag);
        // if (signal == 0x00) {
        // #### 0000
        byte tag = buffer.readByte();
        int len = readVarInt32(buffer, tag);
        if (buffer.readableBytes() < len) {
            throw new IllegalStateException(buffer.readableBytes() + "小于" + len);
        }
        byte[] result = new byte[len];
        buffer.readBytes(result);
        return result;
        // }
        // throw new WrongTypeException();
    }

    @Override
    public void setValue(Context ctx, byte[] value) {
        ByteBuf out = ctx.getBuffer();
        byte flag = Types.BYTE_ARRAY;
        // #### 0000
        out.writeByte(flag);
        int len = value.length;
        putVarInt32(out, len);
        out.writeBytes(value);
    }
}
