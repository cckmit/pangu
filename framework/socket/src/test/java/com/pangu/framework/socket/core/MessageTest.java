package com.pangu.framework.socket.core;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import static org.junit.Assert.*;

public class MessageTest {

    @Test
    public void write() {
        Header header = Header.valueOf((byte) 1, 10, 0xFFFF, 0xFFFFFF, new Command((short) 0, (short) 0));

        byte[] body = new byte[]{1, 2, 3, 4, 5, 6, 7};
        byte[] attachment = new byte[]{1, 2, 3, 4, 5, 6, 7};

        Message message = Message.valueOf(header, body, attachment);

        ByteBuf buffer = Unpooled.buffer();
        message.write(buffer);

        Message after = Message.valueOf(buffer);
        assertEquals(after, message);

        buffer.release();
    }

    @Test
    public void test_valueOf_null_body_null_attachment() {
        Header header = Header.valueOf((byte) 1, 10, 0xFFFF, 0xFFFFFF, new Command((short) 0, (short) 0));

        byte[] body = null;
        byte[] attachment = null;

        Message message = Message.valueOf(header, body, attachment);

        ByteBuf buffer = Unpooled.buffer();
        message.write(buffer);

        Message after = Message.valueOf(buffer);
        assertEquals(after, message);

        buffer.release();
    }

    @Test
    public void test_valueOf_null_attachment() {
        Header header = Header.valueOf((byte) 1, 10, 0xFFFF, 0xFFFFFF, new Command((short) 0, (short) 0));

        byte[] body = new byte[]{1, 2, 3, 4, 5, 6, 7};
        byte[] attachment = null;

        Message message = Message.valueOf(header, body, attachment);

        ByteBuf buffer = Unpooled.buffer();
        message.write(buffer);

        Message after = Message.valueOf(buffer);
        assertEquals(after, message);

        buffer.release();
    }
}