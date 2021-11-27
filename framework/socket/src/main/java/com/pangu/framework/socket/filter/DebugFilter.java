package com.pangu.framework.socket.filter;

import com.pangu.framework.utils.codec.CryptUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;

/**
 * DEBUG过滤器
 */
@Sharable
@Slf4j
public class DebugFilter extends ChannelDuplexHandler implements SocketFilter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (log.isDebugEnabled()) {
            if (msg instanceof ByteBuf) {
                ByteBuf buf = (ByteBuf) msg;
                buf.markReaderIndex();
                byte[] bytes = new byte[buf.readableBytes()];
                buf.readBytes(bytes);
                buf.resetReaderIndex();
                log.debug("RECV - {}\n{}", msg, CryptUtils.byte2hex(bytes, " "));
            } else {
                log.debug("RECV - {}", msg);
            }
        }
        super.channelRead(ctx, msg);

    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (log.isDebugEnabled()) {
            if (msg instanceof ByteBuf) {
                ByteBuf buf = (ByteBuf) msg;
                buf.markReaderIndex();
                byte[] bytes = new byte[buf.readableBytes()];
                buf.readBytes(bytes);
                buf.resetReaderIndex();
                log.debug("SEND - {}\n{}", msg, CryptUtils.byte2hex(bytes, " "));
            } else {
                log.debug("SEND - {}", msg);
            }
        }
        super.write(ctx, msg, promise);
    }

    @Override
    public int getIndex() {
        return DEBUG;
    }

    @Override
    public String getName() {
        return DEBUG_NAME;
    }
}
