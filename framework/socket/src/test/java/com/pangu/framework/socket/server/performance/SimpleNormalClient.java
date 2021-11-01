package com.pangu.framework.socket.server.performance;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;

public class SimpleNormalClient {

    private final Bootstrap b;
    private final EventLoopGroup workerGroup;

    SimpleNormalClient() {
        workerGroup = new NioEventLoopGroup();

        b = new Bootstrap();
        b.group(workerGroup); // (2)
        b.channel(NioSocketChannel.class); // (3)
        b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
        b.option(ChannelOption.TCP_NODELAY, true); // (4)
        b.option(ChannelOption.SO_SNDBUF, 256);
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        ReferenceCountUtil.release(msg);
                    }
                });
            }
        });
    }

    Channel connect(String host, int port) {

        ChannelFuture f = null; // (5)
        try {
            f = b.connect(host, port).sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return f.channel();
    }

    public void stop() {
        workerGroup.shutdownGracefully();
    }
}
