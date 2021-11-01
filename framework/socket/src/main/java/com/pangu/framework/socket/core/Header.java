package com.pangu.framework.socket.core;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.nio.ByteBuffer;

import static com.pangu.framework.socket.core.StateConstant.STATE_RESPONSE;

/**
 * 信息头<br/>
 * 格式:[长度][格式][状态][序号][会话][指令][模块号]
 */
@Data
@EqualsAndHashCode
public class Header {

    /**
     * 默认消息头长度
     */
    public static final int DEFAULT_HEADER_LENGTH = 1 + 4 + 8 + 8 + 4;

    // 格式
    private byte format;
    // 状态
    private int state;
    // 序号
    private long sn;
    // 会话标识
    private long session;
    // 指令
    private Command command;

    // 状态处理方法

    /**
     * 是否存在错误状态
     *
     * @return
     */
    public boolean hasError() {
        return (state >>> 20) != 0;
    }

    /**
     * 检查状态值中是否有指定状态
     *
     * @param check 被检查的状态
     * @return
     */
    public boolean hasState(int check) {
        return (state & check) == check;
    }

    /**
     * 添加状态
     *
     * @param added 被添加的状态
     */
    public void addState(int added) {
        state |= added;
    }

    /**
     * 移除状态
     *
     * @param removed 被移除的状态
     */
    public void removeState(int removed) {
        state &= ~removed;
    }

    @Override
    public String toString() {
        return "[F=" + format + ", ST=" + state + ", SN=" + sn + ", IO=" + session + ", C=" + command + "]";
    }

    /**
     * 是否回应信息
     *
     * @return
     */
    public boolean isResponse() {
        return hasState(STATE_RESPONSE);
    }

    public int getErrorCode() {
        return state >>> 20;
    }

    public void addError(int error) {
        state = (state & 0x000FFFFF) | (error << 20);
    }

    public boolean hasError(int error) {
        return (state >>> 20) == error;
    }

    public void write(ByteBuf out) {
        out.writeByte(format);
        out.writeInt(state);
        out.writeLong(sn);
        out.writeLong(session);
        command.write(out);
    }

    public void read(ByteBuf byteBuf) {
        this.format = byteBuf.readByte();
        this.state = byteBuf.readInt();
        this.sn = byteBuf.readLong();
        this.session = byteBuf.readLong();
        if (this.command == null) {
            this.command = new Command();
        }
        this.command.read(byteBuf);
    }

    public void read(ByteBuffer byteBuf) {
        this.format = byteBuf.get();
        this.state = byteBuf.getInt();
        this.sn = byteBuf.getLong();
        this.session = byteBuf.getLong();
        if (this.command == null) {
            this.command = new Command();
        }
        this.command.read(byteBuf);
    }

    public static Header valueOf(byte format, int state, long sn, long session, Command command) {
        Header result = new Header();
        result.format = format;
        result.state = state;
        result.sn = sn;
        result.session = session;
        result.command = command;
        return result;
    }
}
