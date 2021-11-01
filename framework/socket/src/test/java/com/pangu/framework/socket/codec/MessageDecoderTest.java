package com.pangu.framework.socket.codec;

import com.pangu.framework.socket.core.Command;
import com.pangu.framework.socket.core.Header;
import com.pangu.framework.socket.core.Message;
import com.pangu.framework.socket.exception.SocketException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.TooLongFrameException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MessageDecoderTest {

    private ChannelHandlerContext channelHandlerContext;
    private ByteBuf buffer;
    private Message message;

    @Before
    public void init() {
        channelHandlerContext = mock(ChannelHandlerContext.class);
        Channel channel = mock(Channel.class);
        when(channel.remoteAddress()).thenReturn(new InetSocketAddress(111));
        when(channelHandlerContext.channel()).thenReturn(channel);

        buffer = Unpooled.buffer();

        MessageEncoder messageEncoder = new MessageEncoder();
        Header header = Header.valueOf((byte) 1, 10, 0xFFFF, 0xFFFFFF, new Command((short) 0, (short) 0));

        byte[] body = new byte[]{1, 2, 3, 4, 5, 6, 7};
        byte[] attachment = new byte[]{1, 2, 3, 4, 5, 6, 7};

        message = Message.valueOf(header, body, attachment);

        messageEncoder.encode(channelHandlerContext, message, buffer);
    }

    @After
    public void destory() {
        buffer.release();
    }

    @Test
    public void test_complete_message_decode() throws Exception {
        MessageDecoder decoder = new MessageDecoder();
        Message decode = decoder.decode(channelHandlerContext, buffer);
        assertEquals(decode, this.message);
    }

    @Test(expected = TooLongFrameException.class)
    public void test_error_length_message_decode() throws Exception {
        MessageDecoder decoder = new MessageDecoder();
        buffer.setInt(4, 0xFFFFFFF);
        Message decode = decoder.decode(channelHandlerContext, buffer);
        assertEquals(decode, this.message);
    }

    @Test
    public void test_error_identity_package_message_decode() throws Exception {
        MessageDecoder decoder = new MessageDecoder();
        buffer.setInt(0, 1);
        Message decode = decoder.decode(channelHandlerContext, buffer);
        assertNull(decode);
    }

    @Test
    public void test_error_checksum_package_message_decode() throws Exception {
        MessageDecoder decoder = new MessageDecoder();
        buffer.setInt(buffer.readableBytes() - 4, 1);
        Message decode = decoder.decode(channelHandlerContext, buffer);
        assertNull(decode);
    }

    @Test
    public void test_not_full_message_decode() throws Exception {
        MessageDecoder decoder = new MessageDecoder();
        buffer.writerIndex(buffer.writerIndex() - 10);
        Message decode = decoder.decode(channelHandlerContext, buffer);
        assertNull(decode);
    }
}