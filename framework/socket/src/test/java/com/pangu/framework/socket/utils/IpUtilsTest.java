package com.pangu.framework.socket.utils;

import io.netty.channel.Channel;
import org.junit.Test;

import java.net.InetSocketAddress;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IpUtilsTest {

    @Test
    public void getIp() {
        Channel channel = mock(Channel.class);
        when(channel.remoteAddress()).thenReturn(new InetSocketAddress(2222));
        String ip = IpUtils.getIp(channel);
        System.out.println(ip);
    }

    @Test
    public void getIp1() {
    }

    @Test
    public void isValidIp() {
    }

    @Test
    public void toInetSocketAddress() {
    }

    @Test
    public void isInner() {
    }
}