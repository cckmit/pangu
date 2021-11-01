package com.pangu.framework.socket.filter;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * 腾讯TGW过滤器
 */
@Sharable
public class TGWFilter extends ChannelInboundHandlerAdapter implements SocketFilter {

    private final static Logger logger = LoggerFactory.getLogger(TGWFilter.class);

    // TGW转发请求
    private final static byte[] TGW_HEADER = "tgw".getBytes();
    private final static int TGW_LEN = TGW_HEADER.length;
    // 是否需要处理TGW转发请求
    private final static AttributeKey<Boolean> SEND_TGW = AttributeKey.newInstance("SEND_TGW");

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Attribute<Boolean> attr = ctx.channel().attr(SEND_TGW);
        Boolean sended = attr.get();
        if (sended == null || !sended) {
            if (msg instanceof ByteBuf) {
                ByteBuf in = (ByteBuf) msg;
                // TGW转发请求
                if (in.readableBytes() >= TGW_LEN) {
                    in.markReaderIndex();
                    byte[] dst = new byte[TGW_LEN];
                    in.readBytes(dst);
                    if (!Arrays.equals(dst, TGW_HEADER)) {
                        in.resetReaderIndex();
                    } else {
                        // TGW请求以"\n\n"结尾
                        int len = in.readableBytes();
                        int check = 0;
                        while (in.isReadable()) {
                            if (in.readByte() == '\n') {
                                if (check == 2) {
                                    attr.set(true);
                                    if (logger.isInfoEnabled()) {
                                        logger.info("接受到[{}]TGW转发请求,丢弃余下字节[{}]", ctx.channel().remoteAddress(),
                                                (len - in.readableBytes()));
                                    }
                                    break;
                                } else {
                                    check++;
                                }
                            }
                        }

                    }
                }
            }
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public int getIndex() {
        return TGW;
    }

    @Override
    public String getName() {
        return TGW_NAME;
    }
}
