package com.pangu.framework.socket.handler.param;

import lombok.Data;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertArrayEquals;

public class JsonCoderTest {

    @Test
    public void test_byte_array_raw_1() throws IOException {
        byte[] values = make(1);
        byte[] coder = JsonCoder.MAPPER.writeValueAsBytes(values);
        byte[] decoder = JsonCoder.MAPPER.readValue(coder, byte[].class);
        assertArrayEquals(values, decoder);
    }

    @Test
    public void test_byte_array_raw_2() throws IOException {
        byte[] values = make(2);
        byte[] coder = JsonCoder.MAPPER.writeValueAsBytes(values);
        byte[] decoder = JsonCoder.MAPPER.readValue(coder, byte[].class);
        assertArrayEquals(values, decoder);
    }

    @Test
    public void test_byte_array_raw_4() throws IOException {
        byte[] values = make(4);
        byte[] coder = JsonCoder.MAPPER.writeValueAsBytes(values);
        byte[] decoder = JsonCoder.MAPPER.readValue(coder, byte[].class);
        assertArrayEquals(values, decoder);
    }

    @Test
    public void test_byte_array_obj_1() throws IOException {
        Wrap wrap = makeObj(1);
        byte[] coder = JsonCoder.MAPPER.writeValueAsBytes(wrap);
        Wrap decoder = JsonCoder.MAPPER.readValue(coder, Wrap.class);
        assertArrayEquals(wrap.bytes, decoder.bytes);
    }

    @Test
    public void test_byte_array_obj_2() throws IOException {
        Wrap wrap = makeObj(2);
        byte[] coder = JsonCoder.MAPPER.writeValueAsBytes(wrap);
        Wrap decoder = JsonCoder.MAPPER.readValue(coder, Wrap.class);
        assertArrayEquals(wrap.bytes, decoder.bytes);
    }

    @Test
    public void test_byte_array_obj_4() throws IOException {
        Wrap wrap = makeObj(4);
        byte[] coder = JsonCoder.MAPPER.writeValueAsBytes(wrap);
        Wrap decoder = JsonCoder.MAPPER.readValue(coder, Wrap.class);
        assertArrayEquals(wrap.bytes, decoder.bytes);
    }

    private byte[] make(int type) {
        byte[] bytes = new byte[4096];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        for (int i = 1; i <= 4096 / type; ++i) {
            switch (type) {
                case 1:
                    buffer.put((byte) i);
                    break;
                case 2:
                    buffer.putShort((short) i);
                    break;
                case 4:
                    buffer.putInt(i);
                    break;
            }
        }
        return bytes;
    }

    private Wrap makeObj(int type) {
        byte[] bytes = new byte[4096];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        for (int i = 1; i <= 4096 / type; ++i) {
            switch (type) {
                case 1:
                    buffer.put((byte) i);
                    break;
                case 2:
                    buffer.putShort((short) i);
                    break;
                case 4:
                    buffer.putInt(i);
                    break;
            }
        }
        return new Wrap(bytes);
    }

    @Data
    private static class Wrap {
        byte[] bytes;

        public Wrap() {
        }

        public Wrap(byte[] bytes) {
            this.bytes = bytes;
        }
    }
}