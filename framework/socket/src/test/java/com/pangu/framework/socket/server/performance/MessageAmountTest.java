package com.pangu.framework.socket.server.performance;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.util.ArrayList;
import java.util.Arrays;

public class MessageAmountTest {

    public static void main(String[] args) throws Exception {
        int port = 18888;
        SimpleNormalServer simpleServer = new SimpleNormalServer(port);
        simpleServer.start();

        SimpleNormalClient simpleClient = new SimpleNormalClient();
        int connect = 1024;
        ArrayList<Channel> channels = new ArrayList<>(connect);
        for (int i = 0; i < connect; ++i) {
            Channel channel = simpleClient.connect("192.168.11.242", port);
            channels.add(channel);
        }
        PooledByteBufAllocator allocator = PooledByteBufAllocator.DEFAULT;
        int size = 1024 * 1024;
        byte[] bytes = new byte[size];
        Arrays.fill(bytes, (byte) 1);
        long count = 0;
        while ((++count) < 2) {
            for (Channel channel : channels) {
                ByteBuf byteBuf = allocator.ioBuffer(size);
                byteBuf.writeBytes(bytes);
                channel.writeAndFlush(byteBuf);
            }
        }
        int read = System.in.read();
        Thread.sleep(1000);
        for (Channel channel : channels) {
            ChannelFuture close = channel.close();
            close.sync();
        }
    }
}
