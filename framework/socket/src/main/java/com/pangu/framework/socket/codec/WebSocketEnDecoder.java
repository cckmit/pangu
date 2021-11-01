package com.pangu.framework.socket.codec;

import com.pangu.framework.socket.core.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebSocketEnDecoder extends ChannelDuplexHandler {

    // 解析二进制数据
    private final MessageDecoder messageDecoder = new MessageDecoder();
    private final MessageEncoder messageEncoder = new MessageEncoder();

    @Override

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //二进制消息
        if (msg instanceof BinaryWebSocketFrame) {
            ByteBuf content = ((BinaryWebSocketFrame) msg).content();
            debugLog("websocket receive", content);
            messageDecoder.channelRead(ctx, content);
            return;
        }
        if (msg instanceof ContinuationWebSocketFrame) {
            ByteBuf content = ((ContinuationWebSocketFrame) msg).content();
            debugLog("websocket receive", content);
            messageDecoder.channelRead(ctx, content);
            return;
        }
        //文本消息
        if (msg instanceof TextWebSocketFrame) {
            ((TextWebSocketFrame) msg).release();
            Channel channel = ctx.channel();
            channel.close();
            return;
        }
        //ping消息
        if (msg instanceof PingWebSocketFrame) {
            ((PingWebSocketFrame) msg).release();
            return;
        }
        if (msg instanceof PongWebSocketFrame) {
            ((PongWebSocketFrame) msg).release();
            return;
        }
        //关闭消息
        if (msg instanceof CloseWebSocketFrame) {
            ((CloseWebSocketFrame) msg).release();
            Channel channel = ctx.channel();
            channel.close();
            return;
        }
        if (msg instanceof ByteBufHolder) {
            ((ByteBufHolder) msg).release();
            Channel channel = ctx.channel();
            channel.close();
            return;
        }
        if (msg instanceof ByteBuf) {
            ((ByteBuf) msg).release();
            Channel channel = ctx.channel();
            channel.close();
            return;
        }
        Channel channel = ctx.channel();
        channel.close();
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof Message) {
            ByteBuf buffer = ctx.alloc().buffer();

            messageEncoder.encode((Message) msg, buffer);

            debugLog("websocket write", buffer);

            BinaryWebSocketFrame socketFrame = new BinaryWebSocketFrame(buffer);
            ctx.write(socketFrame, promise);
            return;
        }
        super.write(ctx, msg, promise);
    }

    private void debugLog(String msg, ByteBuf buffer) {
        if (log.isDebugEnabled()) {
            int writerIndex = buffer.writerIndex();
            StringBuilder builder = new StringBuilder(writerIndex * 3);
            for (int i = 0; i < writerIndex; ++i) {
                builder.append(buffer.getByte(i)).append(" ");
            }
            log.debug(writerIndex + msg + builder.toString());
        }
    }
}
