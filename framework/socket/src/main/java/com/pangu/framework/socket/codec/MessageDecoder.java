package com.pangu.framework.socket.codec;

import com.dianping.cat.Cat;
import com.pangu.framework.socket.utils.CheckSumUtils;
import com.pangu.framework.socket.core.Message;
import com.pangu.framework.socket.core.StateConstant;
import com.pangu.framework.socket.exception.ExceptionCode;
import com.pangu.framework.socket.exception.SocketException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageDecoder extends LengthFieldBasedFrameDecoder {

    public MessageDecoder() {
        super(100 * 1024 * 1024, 4, 4, 0, 0);
    }

    @Override
    protected ByteBuf extractFrame(ChannelHandlerContext ctx, ByteBuf buffer, int index, int length) {
        return buffer.slice(index, length);
    }

    @Override
    public Message decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        if (in == null || !in.isReadable()) {
            // BUFF为空
            return null;
        }
        if (in.readableBytes() < 4) {
            return null;
        }
        int preCheckPackageHeader = in.getInt(0);
        // 包头
        if (preCheckPackageHeader != StateConstant.PACKAGE_IDENTITY_PREFIX) {
//            throw new SocketException(ExceptionCode.INVALID_MESSAGE);
            // 包头不匹配
            log.warn("非法数据包 - 标识[0x{}]不匹配", String.format("%X", preCheckPackageHeader));
            in.skipBytes(in.readableBytes());
            ctx.close();
            return null;
        }
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            return null;
        }
        if (!frame.isReadable()) {
            return null;
        }
        int header = frame.readInt();
        // 包头
        if (header != StateConstant.PACKAGE_IDENTITY_PREFIX) {
            // 包头不匹配
            log.warn("非法数据包 - 标识[0x{}]不匹配", String.format("%X", header));
//            throw new SocketException(ExceptionCode.INVALID_MESSAGE);
            in.skipBytes(in.readableBytes());
            ctx.close();
            return null;
        }

        if (frame.readableBytes() < 4) {// 包头不匹配
            throw new SocketException(ExceptionCode.INVALID_MESSAGE);
        }

        int length = frame.readInt();
        // 真正包数据长度减去校验码4Byte
        length -= 4;
        if (frame.readableBytes() < length) {
//            throw new SocketException(ExceptionCode.INVALID_MESSAGE);
            log.warn("数据包长度异常[{}][{}][{}]", ctx.channel(), frame.readableBytes(), length);
            in.skipBytes(in.readableBytes());
            ctx.close();
            return null;
        }

        ByteBuf slice = frame.slice(frame.readerIndex(), length);

        frame.skipBytes(length);

        // 校验码比较
        int checksum = frame.readInt();
        int hashcode = CheckSumUtils.checkSum(slice);

        if (checksum != hashcode) {
            if (log.isInfoEnabled()) {
                log.info("校验码[{}]错误需要[{}]", checksum, hashcode);
            }
//            throw new SocketException(ExceptionCode.INVALID_MESSAGE);
            log.warn("数据包校验码异常[{}][{}][{}]", ctx.channel(), checksum, hashcode);
            in.skipBytes(in.readableBytes());
            ctx.close();
            return null;
        }

        Cat.logMetricForCount("socket.decode", slice.readableBytes());
        // 将数据转为消息对象
        return Message.valueOf(slice);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof DecoderException) {
            ctx.close();
            log.info("解码异常" + ctx.channel() + cause.getMessage());
            return;
        }
        super.exceptionCaught(ctx, cause);
    }
}
