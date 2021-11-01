package com.pangu.framework.socket.server;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;

public class SocketServerBuilderTest {

    @Test
    public void test_address() {
        SocketServerBuilder builder = new SocketServerBuilder();

        builder.address("123");
        assertThat(builder.getPort(), is(123));
        assertNull(builder.getIps());

        builder.address(":123");
        assertThat(builder.getPort(), is(123));
        assertNull(builder.getIps());

        builder.address("localhost:123");
        assertThat(builder.getPort(), is(123));
        assertArrayEquals(builder.getIps(), new String[]{"localhost"});

        builder.address("0.0.0.0:123");
        assertThat(builder.getPort(), is(123));
        assertArrayEquals(builder.getIps(), new String[]{"0.0.0.0"});

        builder.address("192.168.11.1:123");
        assertThat(builder.getPort(), is(123));
        assertArrayEquals(builder.getIps(), new String[]{"192.168.11.1"});

        builder.address("localhost,192.168.11.1:123");
        assertThat(builder.getPort(), is(123));
        assertArrayEquals(builder.getIps(), new String[]{"localhost","192.168.11.1"});
    }

    @Test
    public void test_build_server() {
        SocketServerBuilder builder = new SocketServerBuilder();
        SocketServer socketServer = builder.port(50000).build();
        socketServer.start();
        socketServer.stop();
    }

}