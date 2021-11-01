package com.pangu.framework.socket.utils;

import io.netty.buffer.ByteBuf;

public abstract class CheckSumUtils {
    public static int checkSum(ByteBuf byteBuf) {
        long hashcode = 0;

        int length = byteBuf.readableBytes();
        for (int i = 0; i < length; i++) {
            hashcode = hashcode << 7 ^ byteBuf.getByte(i);
        }
        return (int) hashcode;
    }
}
