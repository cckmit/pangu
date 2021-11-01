package com.pangu.framework.protocol.proxy;

import com.pangu.framework.protocol.exception.UnknowSignalException;
import com.pangu.framework.protocol.exception.WrongTypeException;
import com.pangu.framework.protocol.Context;
import com.pangu.framework.protocol.Types;
import com.pangu.framework.utils.codec.QuickLZUtils;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class StringProxy extends AbstractProxy<String> {

    /**
     * 是否对自动压缩字符串
     */
    private boolean autoCompress = false;
    private int autoSize = 64;

    public boolean isAutoCompress() {
        return autoCompress;
    }

    public void setAutoCompress(boolean autoCompress) {
        this.autoCompress = autoCompress;
    }

    public int getAutoSize() {
        return autoSize;
    }

    public void setAutoSize(int autoSize) {
        this.autoSize = autoSize;
    }

    public String getValue(Context ctx, byte flag) {
        ByteBuf buffer = ctx.getBuffer();
        byte type = getFlagTypes(flag);
        if (type != Types.STRING) {
            throw new WrongTypeException(Types.STRING, type);
        }

        byte signal = getFlagSignal(flag);
        if (signal == 0x00) {
            // #### 0000
            byte tag = buffer.readByte();
            int len = readVarInt32(buffer, tag);
            if (buffer.readableBytes() < len) {
                throw new IllegalStateException(buffer.readableBytes() + "小于" + len);
            }
            byte[] buf = new byte[len];
            buffer.readBytes(buf);
            String result = new String(buf, StandardCharsets.UTF_8);
            return result;
        } else if (signal == 0x02) {
            // #### 0010
            byte tag = buffer.readByte();
            int len = readVarInt32(buffer, tag);
            if (buffer.readableBytes() < len) {
                throw new IllegalStateException();
            }
            byte[] buf = new byte[len];
            buffer.readBytes(buf);
            // 压缩的字符串
            byte[] unzip = QuickLZUtils.unzip(buf, 30, TimeUnit.SECONDS);
            return new String(unzip, StandardCharsets.UTF_8);
        }
        throw new UnknowSignalException(type, signal);
    }

    public void setValue(Context ctx, String value) {
        ByteBuf out = ctx.getBuffer();
        byte flag = Types.STRING;
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (isAutoCompress() && bytes.length > getAutoSize()) {
            flag |= 0x02;
            bytes = QuickLZUtils.zip(bytes);
        }
        out.writeByte(flag);

        int len = bytes.length;
        putVarInt32(out, len);
        out.writeBytes(bytes);
    }

}
