package com.pangu.framework.socket.server;

import com.pangu.framework.socket.codec.ServerMultiProtocolSwitchHandler;
import com.pangu.framework.socket.filter.HeartBeatFilter;
import com.pangu.framework.socket.filter.ServerHandler;
import com.pangu.framework.socket.filter.SocketFilter;
import com.pangu.framework.socket.handler.DefaultDispatcher;
import com.pangu.framework.socket.handler.Dispatcher;
import com.pangu.framework.socket.handler.SessionManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLEngine;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class ServerInitializer extends ChannelInitializer<SocketChannel> {

    private Dispatcher dispatcher;

    private SessionManager sessionManager;

    private SslContext sslContext;

    private List<SocketFilter> filters;

    private final ServerHandler serverHandler;

    private final HeartBeatFilter heartBeatFilter = new HeartBeatFilter();

    ServerInitializer(Dispatcher dispatcher, SessionManager sessionManager, SslContext sslContext, List<SocketFilter> filters) {
        this.dispatcher = dispatcher;
        this.sessionManager = sessionManager;
        this.sslContext = sslContext;
        this.filters = filters;
        this.filters.sort(Comparator.comparingInt(SocketFilter::getIndex));
        serverHandler = new ServerHandler(sessionManager, dispatcher);
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        if (sslContext != null) {
            final String hotCheckSslProtocol = "assert-ssl-handler";
            pipeline.addFirst(hotCheckSslProtocol, new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    if (!(msg instanceof ByteBuf)) {
                        super.channelRead(ctx, msg);
                        return;
                    }
                    ByteBuf byteBuf = (ByteBuf) msg;

                    int readableBytes = byteBuf.readableBytes();
                    if (readableBytes < 2) {
                        return;
                    }
                    short sslHead = byteBuf.getShort(0);
                    ChannelPipeline currentPipLine = ctx.pipeline();
                    // 0x1603 ssl协议ClientHello协议头
                    if (sslHead != 0x1603) {
                        currentPipLine.remove(hotCheckSslProtocol);
                        super.channelRead(ctx, msg);
                        return;
                    }
                    ByteBufAllocator alloc = ch.alloc();
                    SSLEngine sslEngine = sslContext.newEngine(alloc);
                    sslEngine.setUseClientMode(false);
                    sslEngine.setNeedClientAuth(false);
                    currentPipLine.addAfter(hotCheckSslProtocol, "ssl", new SslHandler(sslEngine));
                    currentPipLine.remove(hotCheckSslProtocol);
                    super.channelRead(ctx, msg);
                }
            });
        }
        // 过滤器
        if (filters != null) {
            for (SocketFilter filter : filters) {
                String key = filter.getName();
                try {
                    if (pipeline.get(key) != null) {
                        continue;
                    }
                    pipeline.addLast(key, filter);
                    if (log.isDebugEnabled()) {
                        log.debug("连接[{}]添加过滤器[{}]", ch.remoteAddress(), key);
                    }
                } catch (Exception ex) {
                    log.error("连接[{}]添加过滤器异常!", ch.remoteAddress(), ex);
                    throw ex;
                }

            }
        }

        // 编码解码器
        pipeline.addLast("CODEC", new ServerMultiProtocolSwitchHandler());
        pipeline.addLast("HEARTBEAT", heartBeatFilter);
        pipeline.addLast("HANDLER", serverHandler);
    }
}
