package com.pangu.framework.socket.codec;

import com.pangu.framework.socket.core.Command;
import com.pangu.framework.socket.core.Header;
import com.pangu.framework.socket.core.Message;
import com.pangu.framework.socket.utils.CheckSumUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class MessageEncoderTest {

    private ByteBuf data;
    private ByteBuf buffer;
    private MessageEncoder messageEncoder;
    private ChannelHandlerContext channelHandlerContext;

    @Before
    public void setUp() throws Exception {
        data = Unpooled.buffer();
        buffer = Unpooled.buffer();
        messageEncoder = new MessageEncoder();
        channelHandlerContext = mock(ChannelHandlerContext.class);
    }

    @After
    public void tearDown() throws Exception {
        data.release();
        buffer.release();
    }

    @Test
    public void test_encode_byte_buf() {
        data.writeInt(1);
        messageEncoder.encode(channelHandlerContext, data, buffer);
        assertThat(buffer.readableBytes(), is(4));
    }

    @Test
    public void test_encode_byte_array() {
        byte[] bytes = {1, 2, 3, 4};

        messageEncoder.encode(channelHandlerContext, bytes, buffer);

        byte[] outBuf = new byte[4];
        buffer.readBytes(outBuf);

        assertThat(buffer.readableBytes(), is(0));

        assertArrayEquals(bytes, outBuf);
    }

    @Test
    public void test_encode_message() {
        MessageEncoder messageEncoder = new MessageEncoder();
        ChannelHandlerContext channelHandlerContext = mock(ChannelHandlerContext.class);

        Header header = Header.valueOf((byte) 1, 10, 0xFFFF, 0xFFFFFF, new Command((short) 0, (short) 0));

        byte[] body = new byte[]{1, 2, 3, 4, 5, 6, 7};
        byte[] attachment = new byte[]{1, 2, 3, 4, 5, 6, 7};

        Message message = Message.valueOf(header, body, attachment);
        ByteBuf dataBuff = Unpooled.buffer();
        message.write(dataBuff);

        ByteBuf buffer = Unpooled.buffer();

        messageEncoder.encode(channelHandlerContext, message, buffer);

        // 包头校验
        int packagePrefix = buffer.getInt(0);
        assertThat(packagePrefix, is(0xFFFFFFFF));

        // 数据长度校验
        int dataLength = buffer.readableBytes() - 4;
        assertThat(dataLength, is(dataBuff.readableBytes()));

        // 读取数据内容
        ByteBuf readData = Unpooled.buffer(dataLength);
        buffer.getBytes(4, readData, dataLength);
        assertEquals(dataBuff, readData);

        // 校验码校验
        int checksum = buffer.getInt(4 + 4 + dataBuff.readableBytes());
        int originChecksum = CheckSumUtils.checkSum(readData);
        assertThat(checksum, is(originChecksum));
    }
}