package com.pangu.framework.socket.core;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;

/**
 * 通信信息对象
 *
 * <pre>
 * 包：包头[包标识+包长度]+通信信息
 *
 * 通信信息：[信息头][信息体][附加内容]
 * 信息头:[长度][格式][状态][序号][会话][模块号][指令]
 * 信息体:[长度][内容]
 * 附加内容:[内容]
 * </pre>
 */
@Slf4j
@Data
@EqualsAndHashCode
public class Message {

    // 信息头
    private Header header;

    //  信息体
    private byte[] body = ArrayUtils.EMPTY_BYTE_ARRAY;

    // 通信附加信息体
    private byte[] attachment = ArrayUtils.EMPTY_BYTE_ARRAY;

    public static Message valueOf(Header header) {
        return valueOf(header, null, null);
    }

    public static Message valueOf(Header header, byte[] body, byte[] attachment) {
        Message result = new Message();
        result.header = header;
        if (body != null) {
            result.body = body;
        }
        if (attachment != null) {
            result.attachment = attachment;
        }
        return result;
    }

    public static Message valueOf(ByteBuf byteBuf) {
        Header header = new Header();
        header.read(byteBuf);
        byte[] body = ArrayUtils.EMPTY_BYTE_ARRAY;
        int readableBytes = byteBuf.readableBytes();
        if (readableBytes >= 4) {
            int bodyLength = byteBuf.readInt();
            body = new byte[bodyLength];
            if (bodyLength > 0) {
                byteBuf.readBytes(body);
            }
        }

        byte[] attachment = ArrayUtils.EMPTY_BYTE_ARRAY;
        readableBytes = byteBuf.readableBytes();
        if (readableBytes >= 4) {
            int attachLength = byteBuf.readInt();
            attachment = new byte[attachLength];
            if (attachLength > 0) {
                byteBuf.readBytes(attachment);
            }
        }
        return valueOf(header, body, attachment);
    }

    public static Message valueOf(ByteBuffer byteBuf) {
        Header header = new Header();
        header.read(byteBuf);
        byte[] body = ArrayUtils.EMPTY_BYTE_ARRAY;
        int readableBytes = byteBuf.remaining();
        if (readableBytes >= 4) {
            int bodyLength = byteBuf.getInt();
            body = new byte[bodyLength];
            if (bodyLength > 0) {
                byteBuf.get(body);
            }
        }

        byte[] attachment = ArrayUtils.EMPTY_BYTE_ARRAY;
        readableBytes = byteBuf.remaining();
        if (readableBytes >= 4) {
            int attachLength = byteBuf.getInt();
            attachment = new byte[attachLength];
            if (attachLength > 0) {
                byteBuf.get(attachment);
            }
        }
        return valueOf(header, body, attachment);
    }

    /**
     * 检查是否有指定状态
     *
     * @param checked 状态标识
     * @return
     */
    public boolean hasState(int checked) {
        return header.hasState(checked);
    }

    /**
     * 添加状态
     *
     * @param added 被添加的状态
     */
    public void addState(int added) {
        header.addState(added);
    }

    public void updateSn(long sn) {
        header.setSn(sn);
    }

    public void updateSession(long sessionId) {
        header.setSession(sessionId);
    }

    /**
     * 移除状态
     *
     * @param removed 被移除的状态
     */
    public void removeState(int removed) {
        header.removeState(removed);
    }

    @Override
    public String toString() {
        return "H:=" + header + " B:" + (body == null ? 0 : body.length) + " A:"
                + (attachment == null ? 0 : attachment.length);
    }

    public void write(ByteBuf out) {
        header.write(out);
        if (body != null && body.length > 0) {
            out.writeInt(body.length);
            out.writeBytes(body);
        } else if (attachment != null && attachment.length > 0) {
            out.writeInt(0);
        }
        if (attachment != null && attachment.length > 0) {
            out.writeInt(attachment.length);
            out.writeBytes(attachment);
        }
    }
}
