package com.pangu.framework.protocol.proxy;

import com.pangu.framework.protocol.exception.UnknowSignalException;
import com.pangu.framework.protocol.exception.WrongTypeException;
import com.pangu.framework.protocol.Context;
import com.pangu.framework.protocol.Types;
import io.netty.buffer.ByteBuf;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class NumberProxy extends AbstractProxy<Number> {

    public static byte INT32 = 0x01;
    public static byte INT64 = 0x02;
    public static byte FLOAT = 0x03;
    public static byte DOUBLE = 0x04;

    private final StringProxy stringProxy = new StringProxy();

    // 0000 1000
    public static final int FLAG_NEVIGATE = 0x08;

    @Override
    public Number getValue(Context ctx, byte flag) {
        ByteBuf buffer = ctx.getBuffer();
        byte type = getFlagTypes(flag);
        if (type != Types.NUMBER) {
            throw new WrongTypeException(Types.NUMBER, type);
        }

        // 0000 #000
        boolean nevigate = ((flag & FLAG_NEVIGATE) != 0);

        // 0000 0###
        byte signal = getFlagSignal(flag);
        if (signal == INT32) {
            byte tag = buffer.readByte();
            int value = readVarInt32(buffer, tag);
            return nevigate ? (value == 0 ? Integer.MIN_VALUE : -value) : value;
        } else if (signal == INT64) {
            byte tag = buffer.readByte();
            long value = readVarInt64(buffer, tag);
            return nevigate ? (value == 0 ? Long.MIN_VALUE : -value) : value;
        } else if (signal == FLOAT) {
            return buffer.readFloat();
        } else if (signal == DOUBLE) {
            return buffer.readDouble();
        }
        throw new UnknowSignalException(type, signal);
    }

    @Override
    public void setValue(Context ctx, Number value) {
        ByteBuf out = ctx.getBuffer();
        byte flag = Types.NUMBER;
        if (value instanceof Integer || value instanceof Short || value instanceof Byte
                || value instanceof AtomicInteger) {
            int v = value.intValue();
            if (v < 0) {
                flag |= FLAG_NEVIGATE | INT32;
                if (v == Integer.MIN_VALUE) {
                    v = 0;
                } else {
                    v = -v;
                }
            } else {
                flag |= INT32;
            }
            out.writeByte(flag);
            putVarInt32(out, v);
        } else if (value instanceof Long || value instanceof AtomicLong || value instanceof BigInteger) {
            long v = value.longValue();

            if (Math.abs(v) > 0xFF_FF_FF_FF_FF_FFL) {
                stringProxy.setValue(ctx, String.valueOf(v));
                return;
            }
            if (v < 0) {
                flag |= FLAG_NEVIGATE | INT64;
                if (v == Long.MIN_VALUE) {
                    v = 0;
                } else {
                    v = -v;
                }
            } else {
                flag |= INT64;
            }
            out.writeByte(flag);
            putVarInt64(out, v);
        } else if (value instanceof Float) {
            float v = value.floatValue();
            flag |= FLOAT;
            out.writeByte(flag);
            out.writeFloat(v);
        } else if (value instanceof Double || value instanceof BigDecimal) {
            double v = value.doubleValue();
            flag |= DOUBLE;
            out.writeByte(flag);
            out.writeDouble(v);
        }
    }

}
