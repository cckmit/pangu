package com.pangu.framework.protocol.proxy;

import com.pangu.framework.protocol.exception.MalformedVarintException;
import com.pangu.framework.protocol.Context;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractProxy<T> implements Proxy<T> {

    protected Logger log = LoggerFactory.getLogger(getClass());

    // 1111 0000
    public static byte TYPE_MASK = (byte) 0xF0;

    // 0000 0111
    public static byte SIGNAL_MASK = (byte) 0x07;

    // 0000 1111
    public static byte NUMBER_BITS = (byte) 0x0F;

    // 1000 0000
    public static final int NUMBER_FLAGS = 0x80;

    /**
     * Types
     *
     * @return #### 0000
     */
    public static byte getFlagTypes(byte flag) {
        byte code = (byte) (flag & TYPE_MASK);
        if (code == 0) {
            return flag;
        }
        return code;
    }

    /**
     * Signal
     *
     * @return 0000 0###
     */
    public static byte getFlagSignal(byte flag) {
        byte signal = (byte) (flag & SIGNAL_MASK);
        return signal;
    }

    public static int readVarInt32(ByteBuf byteBuf, byte tag) {
        // 1### #### (128 - (byte)0x80)
        if ((tag & NUMBER_FLAGS) == 0) {
            return tag & 0x7F;
        }

        int signal = tag & NUMBER_BITS;
        if (byteBuf.readableBytes() < signal) {
            throw new IllegalStateException(byteBuf.readableBytes() + "小于" + signal);
        }

        if (signal > 4 || signal < 0) {
            throw new MalformedVarintException(4, signal);
        }

        int result = 0;
        for (int i = 8 * (signal - 1); i >= 0; i -= 8) {
            byte b = byteBuf.readByte();
            result |= (b & 0xFF) << i;
        }
        return result;
    }

    public static void putVarInt32(ByteBuf out, int value) {
        if (value < 0) {
            // 不能 < 0
            throw new MalformedVarintException(value);
        }

        // 1### #### (128 - (byte)0x80)
        if (value < NUMBER_FLAGS) {
            byte b = (byte) value;
            out.writeByte(b);
        } else if (value <= Integer.MAX_VALUE) {
            // VarInt32
            if ((value >>> 24) > 0) {
                byte b = (byte) (NUMBER_FLAGS | 4);
                out.writeByte(b);
                //
                byte b1 = (byte) (value >>> 24 & 0xFF);
                byte b2 = (byte) (value >>> 16 & 0xFF);
                byte b3 = (byte) (value >>> 8 & 0xFF);
                byte b4 = (byte) (value & 0xFF);
                out.writeByte(b1);
                out.writeByte(b2);
                out.writeByte(b3);
                out.writeByte(b4);
            } else if ((value >>> 16) > 0) {
                byte b = (byte) (NUMBER_FLAGS | 3);
                out.writeByte(b);
                //
                byte b2 = (byte) (value >>> 16 & 0xFF);
                byte b3 = (byte) (value >>> 8 & 0xFF);
                byte b4 = (byte) (value & 0xFF);
                out.writeByte(b2);
                out.writeByte(b3);
                out.writeByte(b4);
            } else if ((value >>> 8) > 0) {
                byte b = (byte) (NUMBER_FLAGS | 2);
                out.writeByte(b);
                //
                byte b3 = (byte) (value >>> 8 & 0xFF);
                byte b4 = (byte) (value & 0xFF);
                out.writeByte(b3);
                out.writeByte(b4);
            } else {
                byte b = (byte) (NUMBER_FLAGS | 1);
                out.writeByte(b);
                //
                byte b4 = (byte) (value & 0xFF);
                out.writeByte(b4);
            }
        } else {
            // 不支持
            throw new IllegalArgumentException("VarInt值超过范围");
        }
    }

    public static long readVarInt64(ByteBuf byteBuf, byte tag) {
        // 1### #### (128 - (byte)0x80)
        if ((tag & NUMBER_FLAGS) == 0) {
            return tag & 0x7F;
        }

        int signal = tag & NUMBER_BITS;
        if (byteBuf.readableBytes() < signal) {
            throw new IllegalStateException(byteBuf.readableBytes() + "小于" + signal);
        }

        if (signal > 8 || signal < 0) {
            throw new MalformedVarintException(8, signal);
        }

        long result = 0;
        for (int i = 8 * (signal - 1); i >= 0; i -= 8) {
            byte b = byteBuf.readByte();
            result |= (long) (b & 0xFF) << i;
        }
        return result;
    }

    public static void putVarInt64(ByteBuf out, long value) {
        if (value < 0) {
            // 不能 < 0
            throw new MalformedVarintException(value);
        }

        // 1### #### (128 - (byte)0x80)
        if (value < NUMBER_FLAGS) {
            byte b = (byte) value;
            out.writeByte(b);
        } else if (value <= Integer.MAX_VALUE) {
            // VarInt32
            if ((value >>> 24) > 0) {
                byte b = (byte) (NUMBER_FLAGS | 4);
                out.writeByte(b);
                //
                byte b1 = (byte) (value >>> 24 & 0xFF);
                byte b2 = (byte) (value >>> 16 & 0xFF);
                byte b3 = (byte) (value >>> 8 & 0xFF);
                byte b4 = (byte) (value & 0xFF);
                out.writeByte(b1);
                out.writeByte(b2);
                out.writeByte(b3);
                out.writeByte(b4);
            } else if ((value >>> 16) > 0) {
                byte b = (byte) (NUMBER_FLAGS | 3);
                out.writeByte(b);
                //
                byte b2 = (byte) (value >>> 16 & 0xFF);
                byte b3 = (byte) (value >>> 8 & 0xFF);
                byte b4 = (byte) (value & 0xFF);
                out.writeByte(b2);
                out.writeByte(b3);
                out.writeByte(b4);
            } else if ((value >>> 8) > 0) {
                byte b = (byte) (NUMBER_FLAGS | 2);
                out.writeByte(b);
                //
                byte b3 = (byte) (value >>> 8 & 0xFF);
                byte b4 = (byte) (value & 0xFF);
                out.writeByte(b3);
                out.writeByte(b4);
            } else {
                byte b = (byte) (NUMBER_FLAGS | 1);
                out.writeByte(b);
                //
                byte b4 = (byte) (value & 0xFF);
                out.writeByte(b4);
            }
        } else if (value <= Long.MAX_VALUE) {
            // VarInt64
            if ((value >>> 56) > 0) {
                byte b = (byte) (NUMBER_FLAGS | 8);
                out.writeByte(b);
                //
                byte b0 = (byte) (value >>> 56 & 0xFF);
                byte b1 = (byte) (value >>> 48 & 0xFF);
                byte b2 = (byte) (value >>> 40 & 0xFF);
                byte b3 = (byte) (value >>> 32 & 0xFF);
                byte b4 = (byte) (value >>> 24 & 0xFF);
                byte b5 = (byte) (value >>> 16 & 0xFF);
                byte b6 = (byte) (value >>> 8 & 0xFF);
                byte b7 = (byte) (value & 0xFF);
                out.writeByte(b0);
                out.writeByte(b1);
                out.writeByte(b2);
                out.writeByte(b3);
                out.writeByte(b4);
                out.writeByte(b5);
                out.writeByte(b6);
                out.writeByte(b7);
            } else if ((value >>> 48) > 0) {
                byte b = (byte) (NUMBER_FLAGS | 7);
                out.writeByte(b);
                //
                byte b1 = (byte) (value >>> 48 & 0xFF);
                byte b2 = (byte) (value >>> 40 & 0xFF);
                byte b3 = (byte) (value >>> 32 & 0xFF);
                byte b4 = (byte) (value >>> 24 & 0xFF);
                byte b5 = (byte) (value >>> 16 & 0xFF);
                byte b6 = (byte) (value >>> 8 & 0xFF);
                byte b7 = (byte) (value & 0xFF);
                out.writeByte(b1);
                out.writeByte(b2);
                out.writeByte(b3);
                out.writeByte(b4);
                out.writeByte(b5);
                out.writeByte(b6);
                out.writeByte(b7);
            } else if ((value >>> 40) > 0) {
                byte b = (byte) (NUMBER_FLAGS | 6);
                out.writeByte(b);
                //
                byte b2 = (byte) (value >>> 40 & 0xFF);
                byte b3 = (byte) (value >>> 32 & 0xFF);
                byte b4 = (byte) (value >>> 24 & 0xFF);
                byte b5 = (byte) (value >>> 16 & 0xFF);
                byte b6 = (byte) (value >>> 8 & 0xFF);
                byte b7 = (byte) (value & 0xFF);
                out.writeByte(b2);
                out.writeByte(b3);
                out.writeByte(b4);
                out.writeByte(b5);
                out.writeByte(b6);
                out.writeByte(b7);
            } else if ((value >>> 32) > 0) {
                byte b = (byte) (NUMBER_FLAGS | 5);
                out.writeByte(b);
                //
                byte b3 = (byte) (value >>> 32 & 0xFF);
                byte b4 = (byte) (value >>> 24 & 0xFF);
                byte b5 = (byte) (value >>> 16 & 0xFF);
                byte b6 = (byte) (value >>> 8 & 0xFF);
                byte b7 = (byte) (value & 0xFF);
                out.writeByte(b3);
                out.writeByte(b4);
                out.writeByte(b5);
                out.writeByte(b6);
                out.writeByte(b7);
            } else {
                byte b = (byte) (NUMBER_FLAGS | 4);
                out.writeByte(b);
                //
                byte b4 = (byte) (value >>> 24 & 0xFF);
                byte b5 = (byte) (value >>> 16 & 0xFF);
                byte b6 = (byte) (value >>> 8 & 0xFF);
                byte b7 = (byte) (value & 0xFF);
                out.writeByte(b4);
                out.writeByte(b5);
                out.writeByte(b6);
                out.writeByte(b7);
            }
        } else {
            // 不支持
            throw new IllegalArgumentException("VarInt值超过范围");
        }
    }

    /* (non-Javadoc)
     *
     * @see com.pangu.framework.codec.proxy.Proxy#getValue(com.pangu.framework.codec.CodecContext, byte) */
    public abstract T getValue(Context ctx, byte flag);

    /* (non-Javadoc)
     *
     * @see com.pangu.framework.codec.proxy.Proxy#setValue(com.pangu.framework.codec.CodecContext, T) */
    public abstract void setValue(Context ctx, T value);
}
