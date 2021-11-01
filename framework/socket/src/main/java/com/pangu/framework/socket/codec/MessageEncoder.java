package com.pangu.framework.socket.codec;

import com.dianping.cat.Cat;
import com.pangu.framework.socket.utils.CheckSumUtils;
import com.pangu.framework.socket.core.Message;
import com.pangu.framework.socket.core.StateConstant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageEncoder extends MessageToByteEncoder<Object> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) {
        if (msg == null) {
            return;
        }
        if (msg instanceof ByteBuf) {
            out.writeBytes((ByteBuf) msg);
            return;
        }
        if (msg instanceof byte[]) {
            out.writeBytes((byte[]) msg);
            return;
        }
        if (!(msg instanceof Message)) {
            log.warn("不支持的消息类型[{}]", msg.getClass());
            return;
        }
        encode((Message) msg, out);
        Cat.logMetricForCount("socket.encode", out.readableBytes());
    }

    public void encode(Message msg, ByteBuf out) {
        // 包头 => (前缀 + 版本)
        int header = StateConstant.PACKAGE_IDENTITY_PREFIX;
        out.writeInt(header);

        // 数据内容长度 + 4(签名长度)
        int lengthIndex = out.writerIndex();
        out.writeInt(0);

        int preWriterIndex = out.writerIndex();
        msg.write(out);
        int afterWriterIndex = out.writerIndex();

        int dataSize = afterWriterIndex - preWriterIndex;
        out.setInt(lengthIndex, dataSize + 4);

        ByteBuf slice = out.slice(preWriterIndex, dataSize);

        long hashcode = CheckSumUtils.checkSum(slice);

        out.writeInt((int) hashcode);
    }

}
