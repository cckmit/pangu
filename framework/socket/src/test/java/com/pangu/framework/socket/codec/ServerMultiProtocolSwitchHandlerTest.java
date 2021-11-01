package com.pangu.framework.socket.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ServerMultiProtocolSwitchHandlerTest {

    private ServerMultiProtocolSwitchHandler switchHandler;
    private ByteBuf buffer;
    private ChannelPipeline channelPipeline;
    private ChannelHandlerContext channelHandlerContext;
    private Channel channel;

    @Before
    public void before() {
        switchHandler = new ServerMultiProtocolSwitchHandler();
        buffer = Unpooled.buffer();

        channelHandlerContext = mock(ChannelHandlerContext.class);
        when(channelHandlerContext.name()).thenReturn("mock name");

        channelPipeline = mock(ChannelPipeline.class);
        when(channelHandlerContext.pipeline()).thenReturn(channelPipeline);

        channel = mock(Channel.class);
        when(channelHandlerContext.channel()).thenReturn(channel);
    }

    public void after() {
        buffer.release();
    }

    @Test
    public void test_channel_read_no_select() throws Exception {
        switchHandler.channelRead(channelHandlerContext, buffer);
        assertThat(switchHandler.getByteBuf(), is(buffer));
        verify(channelHandlerContext.pipeline(), never()).addAfter(anyString(), anyString(), any());
    }

    @Test
    public void test_channel_read_to_websocket() throws Exception {
        buffer.writeShort(('G' << 8) + 'E');
        switchHandler.channelRead(channelHandlerContext, buffer);

        ChannelPipeline channelPipeline = channelHandlerContext.pipeline();

        verify(channelPipeline).addAfter(anyString(), eq("http-codec"), any(HttpServerCodec.class));
        verify(channelPipeline).addAfter(anyString(), eq("http-chunked"), any(ChunkedWriteHandler.class));
        verify(channelPipeline).addAfter(anyString(), eq("aggregator"), any(HttpObjectAggregator.class));
        verify(channelPipeline).addAfter(anyString(), eq("websocketProtocol"), any(WebSocketServerProtocolHandler.class));
        verify(channelPipeline).addAfter(anyString(), eq("websocketHandler"), any(WebSocketEnDecoder.class));

        verify(channelPipeline).remove(any(ChannelHandler.class));

        verify(channelHandlerContext).fireChannelRead(buffer);
    }

    @Test
    public void test_channel_read_to_common_socket() throws Exception {
        buffer.writeInt(0xFFFFFFFF);
        switchHandler.channelRead(channelHandlerContext, buffer);
        verify(channelPipeline).addAfter(anyString(), eq("decoder"), any(MessageDecoder.class));
        verify(channelPipeline).addAfter(anyString(), eq("encoder"), any(MessageEncoder.class));

        verify(channelPipeline).remove(any(ChannelHandler.class));

        verify(channelHandlerContext).fireChannelRead(buffer);
    }

    @Test
    public void test_channel_read_error_header() throws Exception {
        buffer.writeBytes(new byte[]{'t', 'e', 's', 't'});
        switchHandler.channelRead(channelHandlerContext, buffer);

        verify(channel).close();
    }
}