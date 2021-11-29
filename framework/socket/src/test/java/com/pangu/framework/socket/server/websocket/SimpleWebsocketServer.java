package com.pangu.framework.socket.server.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class SimpleWebsocketServer {
    private final int port;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    SimpleWebsocketServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        SimpleWebsocketServer server = new SimpleWebsocketServer(11111);
        server.start();
        System.in.read();
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
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("http-codec", new HttpServerCodec());
                        pipeline.addLast("http-chunked", new ChunkedWriteHandler());
                        pipeline.addLast("aggregator", new HttpObjectAggregator(1024 * 1024));
                        pipeline.addLast(new WebSocketServerProtocolHandler("/hello", null, true, 65535));
                        pipeline.addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                //文本消息
                                if (msg instanceof TextWebSocketFrame) {

                                    //获取当前channel绑定的IP地址
                                    InetSocketAddress ipSocket = (InetSocketAddress) ctx.channel().remoteAddress();
                                    String address = ipSocket.getAddress().getHostAddress();
                                    System.out.println("address为:" + address);
                                    String text = ((TextWebSocketFrame) msg).text();
                                    System.out.println("文本信息：" + text);
                                }
                                //二进制消息
                                if (msg instanceof BinaryWebSocketFrame) {
                                    System.out.println("收到二进制消息：" + ((BinaryWebSocketFrame) msg).content().readableBytes());
//                                    BinaryWebSocketFrame binaryWebSocketFrame = new BinaryWebSocketFrame(Unpooled.buffer().writeBytes("hello".getBytes()));
//                                    //给客户端发送的消息
//                                    ctx.channel().writeAndFlush(binaryWebSocketFrame);
                                }
                                //ping消息
                                if (msg instanceof PongWebSocketFrame) {
                                    System.out.println("客户端ping成功");
                                }
                                //关闭消息
                                if (msg instanceof CloseWebSocketFrame) {
                                    System.out.println("客户端关闭，通道关闭");
                                    Channel channel = ctx.channel();
                                    channel.close();
                                }
                            }
                        });
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 1280)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

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
}
