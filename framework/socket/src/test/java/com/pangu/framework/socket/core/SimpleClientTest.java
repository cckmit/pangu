package com.pangu.framework.socket.core;

import com.pangu.framework.socket.anno.Raw;
import com.pangu.framework.socket.anno.SocketCommand;
import com.pangu.framework.socket.anno.SocketModule;
import com.pangu.framework.socket.client.Client;
import com.pangu.framework.socket.client.ClientFactory;
import com.pangu.framework.socket.client.ClientFactoryBuilder;
import com.pangu.framework.socket.handler.Dispatcher;
import com.pangu.framework.socket.server.SocketServer;
import com.pangu.framework.socket.server.SocketServerBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertArrayEquals;

public class SimpleClientTest {
    private static ClientFactory clientFactory;
    private static SocketServer socketServer;
    static AtomicInteger count = new AtomicInteger();

    @Before
    public void init() {
        SocketServerBuilder builder = new SocketServerBuilder();
        socketServer = builder.port(11111).build();
        socketServer.start();
        Dispatcher dispatcher = socketServer.getDispatcher();
        RawFacadeImpl facade = new RawFacadeImpl();
        dispatcher.register(facade);

        ClientFactoryBuilder clientFactoryBuilder = new ClientFactoryBuilder();
        clientFactoryBuilder.address("11111");
        clientFactory = clientFactoryBuilder.build();
        clientFactory.start();
    }

    @After
    public void tearDown() {
        socketServer.stop();
        clientFactory.stop();
    }

    @Test
    public void test_normal() {
        byte[] values = new byte[1024];
        ByteBuffer wrap = ByteBuffer.wrap(values);
        for (int i = 0; i < 1024 / 4; ++i) {
            wrap.putInt(i);
        }

        Client client = clientFactory.connect();
        RawFacade proxy = client.getProxy(RawFacade.class);
        byte[] normal = proxy.normal(values);
        assertArrayEquals(values, normal);

        client.close();
    }

    @Test
    public void test_raw() {
        byte[] values = new byte[1024];
        ByteBuffer wrap = ByteBuffer.wrap(values);
        for (int i = 0; i < 1024 / 4; ++i) {
            wrap.putInt(i);
        }

        Client client = clientFactory.connect();
        RawFacade proxy = client.getProxy(RawFacade.class);
        byte[] normal = proxy.rawData(values);
        assertArrayEquals(values, normal);

        client.close();
    }

    @SocketModule(1)
    private interface RawFacade {
        @SocketCommand(2)
        byte[] normal(byte[] value);

        @SocketCommand(value = 3, raw = @Raw(request = true, response = true))
        byte[] rawData(byte[] value);
    }

    private static class RawFacadeImpl implements RawFacade {

        @Override
        public byte[] normal(byte[] value) {
            return value;
        }

        @Override
        public byte[] rawData(byte[] value) {
            return value;
        }
    }
}
