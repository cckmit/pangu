package com.pangu.framework.socket.core;

import io.netty.buffer.ByteBuf;
import lombok.*;

import java.nio.ByteBuffer;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Command {

    // 模块号
    private short module;

    // 指令号
    private short command;

    public static Command of(int command, int... module) {
        return new Command((short) module[0], (short) command);
    }

    public static Command of(int command, byte... module) {
        return new Command(module[0], (short) command);
    }

    public void write(ByteBuf out) {
        out.writeShort(module);
        out.writeShort(command);
    }

    public void read(ByteBuf byteBuf) {
        this.module = byteBuf.readShort();
        this.command = byteBuf.readShort();
    }

    public void read(ByteBuffer byteBuf) {
        this.module = byteBuf.getShort();
        this.command = byteBuf.getShort();
    }
}
