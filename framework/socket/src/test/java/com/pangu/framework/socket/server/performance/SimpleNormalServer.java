package com.pangu.framework.socket.server.performance;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class SimpleNormalServer {
    private int port;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    SimpleNormalServer(int port) {
        this.port = port;
    }

    void start() throws Exception {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        AtomicLong countRef = new AtomicLong();
        long now = System.currentTimeMillis();
        AtomicReference<Long> time = new AtomicReference<>(now / 1000);
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                ByteBuf in = (ByteBuf) msg;
                                try {
                                    int readableBytes = in.readableBytes();
                                    long now = System.currentTimeMillis() / 1000;
                                    boolean change = time.get() != now;
                                    System.out.println(in.readableBytes());
                                    if (change) {
                                        time.set(now);
                                        System.out.println(countRef.getAndSet(0));
                                    }
                                    countRef.addAndGet(readableBytes);
                                } finally {
                                    ReferenceCountUtil.release(msg);
                                }
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                cause.printStackTrace();
                                ctx.close();
                            }
                        });
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 1280)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.ALLOW_HALF_CLOSURE, false)
                .childOption(ChannelOption.SO_RCVBUF, 8 * 1024)
                .childOption(ChannelOption.SO_SNDBUF, 8 * 1024);

        // Bind and start to accept incoming connections.
        ChannelFuture sync = b.bind(port).sync();
        serverChannel = sync.channel();
        log.info("绑定端口:" + port);
    }

    public void stop() {
        serverChannel.close();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    class Solution {
        public double findMedianSortedArrays(int[] nums1, int[] nums2) {
            int min = 0;
            if (nums1.length == 0 || nums2.length == 0) {
                if (nums1.length == 0) {
                    min = nums2[0];
                } else {
                    min = nums1[0];
                }
            } else {
                min = Math.min(nums1[0], nums2[0]);
            }
            int max = 0;
            if (nums1.length == 0 || nums2.length == 0) {
                if (nums1.length == 0) {
                    max = nums2[nums2.length - 1];
                } else {
                    max = nums1[nums1.length - 1];
                }
            } else {
                max = Math.max(nums1[nums1.length - 1], nums2[nums2.length - 1]);
            }

            return (min + max) * 1.0 / 2;
        }
    }
}
