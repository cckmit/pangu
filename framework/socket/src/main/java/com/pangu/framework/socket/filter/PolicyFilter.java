package com.pangu.framework.socket.filter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Flash策略文件过滤器
 */
@Sharable
public class PolicyFilter extends ChannelInboundHandlerAdapter implements SocketFilter {

    private final static Logger logger = LoggerFactory.getLogger(PolicyFilter.class);

    // FLASH策略文件回应
    private final static byte[] POLICY_RESPONSE = "<?xml version=\"1.0\"?><cross-domain-policy><site-control permitted-cross-domain-policies=\"all\"/><allow-access-from domain=\"*\" to-ports=\"*\"/></cross-domain-policy>\0"
            .getBytes();
    // FLASH策略文件请求
    private final static byte[] POLICY_REQUEST = "<policy-file-request/>".getBytes();
    private final static int POLICY_LEN = POLICY_REQUEST.length;
    // 是否需要处理FLASH策略文件请求
    private final static AttributeKey<Boolean> SEND_POLICY = AttributeKey.newInstance("SEND_POLICY");
    // 是否一连接即返回策略文件
    private boolean sendOnOpen = false;

    public void setSendOnOpen(boolean sendOnOpen) {
        this.sendOnOpen = sendOnOpen;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (sendOnOpen) {
            Channel channel = ctx.channel();
            channel.writeAndFlush(Unpooled.wrappedBuffer(POLICY_RESPONSE));
            if (logger.isDebugEnabled()) {
                logger.debug("向会话[{}]返回策略文件", channel.remoteAddress());
            }
        }
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Attribute<Boolean> attr = ctx.channel().attr(SEND_POLICY);
        Boolean sended = attr.get();
        if (sended == null || !sended) {
            if (msg instanceof ByteBuf) {
                ByteBuf in = (ByteBuf) msg;
                // POLICY请求检测
                if (in.readableBytes() >= POLICY_LEN) {
                    in.markReaderIndex();
                    byte[] dst = new byte[POLICY_LEN];
                    in.readBytes(dst);
                    if (!Arrays.equals(dst, POLICY_REQUEST)) {
                        in.resetReaderIndex();
                    } else {
                        int len = in.readableBytes();
                        while (in.isReadable()) {
                            if (in.readByte() == '\0') {
                                attr.set(true);
                                if (logger.isInfoEnabled()) {
                                    logger.info("接受到[{}]POLICY请求,丢弃字节[{}]", ctx.channel().remoteAddress(),
                                            (len - in.readableBytes()));
                                }
                                break;
                            }
                        }
                        // 返回策略文件
                        Channel channel = ctx.channel();
                        channel.writeAndFlush(Unpooled.wrappedBuffer(POLICY_RESPONSE));
                        if (logger.isDebugEnabled()) {
                            logger.debug("向会话[{}]返回策略文件", channel.remoteAddress());
                        }
                    }
                }
            }
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public int getIndex() {
        return POLICY;
    }

    @Override
    public String getName() {
        return POLICY_NAME;
    }
}
