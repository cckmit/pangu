package com.pangu.framework.socket.codec;

import com.pangu.framework.socket.core.StateConstant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolConfig;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 最初为了考虑第一个包小于4个字节的情况，缓存不足4个自己的数据包，但这样逻辑可能出现
 * 内存泄露的问题，连接断开时，此ByteBuf没有调用release方法，增加了处理情况的难度
 * 考虑到数据包
 * 所以需要增加在连接断开时，释放缓存的数据包
 */
@Slf4j
@Getter
public class ServerMultiProtocolSwitchHandler extends ChannelInboundHandlerAdapter {

    private ByteBuf byteBuf;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof ByteBuf)) {
            super.channelRead(ctx, msg);
            return;
        }
        if (byteBuf == null) {
            byteBuf = (ByteBuf) msg;
        } else {
            byteBuf.writeBytes((ByteBuf) msg);
            ((ByteBuf) msg).release();
        }

        int readableBytes = byteBuf.readableBytes();
        if (readableBytes < 2) {
            return;
        }
        short websocketPackageStart = byteBuf.getShort(0);
        short HANDSHAKE_PREFIX = ('G' << 8) + 'E';
        if (websocketPackageStart == HANDSHAKE_PREFIX) {
            String name = ctx.name();
            ChannelPipeline pipeline = ctx.pipeline();

            pipeline.addAfter(name, "http-codec", new HttpServerCodec(2048, 2048, 2048));
            pipeline.addAfter("http-codec", "http-chunked", new ChunkedWriteHandler());
            pipeline.addAfter("http-chunked", "aggregator", new HttpObjectAggregator(1024));
            WebSocketServerProtocolConfig config = WebSocketServerProtocolConfig.newBuilder()
                    .withUTF8Validator(false)
                    .handshakeTimeoutMillis(3000)
                    .dropPongFrames(true)
                    .sendCloseFrame(null)
                    .build();
            WebSocketServerProtocolHandler handler = new WebSocketServerProtocolHandler(config);
            pipeline.addAfter("aggregator", "websocketProtocol", handler);
            pipeline.addAfter("websocketProtocol", "websocketHandler", new WebSocketEnDecoder());

            pipeline.remove(this);

            super.channelRead(ctx, byteBuf);
            this.byteBuf = null;
            return;
        }
        if (readableBytes < 4) {
            return;
        }
        int commonSocketPackageStart = byteBuf.getInt(0);
        if (commonSocketPackageStart == StateConstant.PACKAGE_IDENTITY_PREFIX) {
            String name = ctx.name();
            ChannelPipeline pipeline = ctx.pipeline();
            pipeline.addAfter(name, "decoder", new MessageDecoder());
            pipeline.addAfter(name, "encoder", new MessageEncoder());

            pipeline.remove(this);
            super.channelRead(ctx, byteBuf);
            this.byteBuf = null;
            return;
        }
        byteBuf.release();
        this.byteBuf = null;
        log.info("不支持的数据协议包头[{}]", Long.toHexString(commonSocketPackageStart));
        ctx.channel().close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (byteBuf != null) {
            byteBuf.release();
        }
        super.channelInactive(ctx);
    }
}
