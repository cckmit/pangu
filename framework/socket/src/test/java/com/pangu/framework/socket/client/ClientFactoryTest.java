package com.pangu.framework.socket.client;

import com.pangu.framework.socket.handler.Dispatcher;
import com.pangu.framework.socket.server.SocketServer;
import com.pangu.framework.socket.server.SocketServerBuilder;
import com.pangu.framework.socket.utils.IpUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClientFactoryTest {
    private static ClientFactory clientFactory;
    private static SocketServer socketServer;

    @Before
    public void init() {
        SocketServerBuilder builder = new SocketServerBuilder();
        socketServer = builder.port(11111).build();
        socketServer.start();
        Dispatcher dispatcher = socketServer.getDispatcher();
        SimpleClientTest.FacadeImpl facade = new SimpleClientTest.FacadeImpl();
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
    public void test_heart_beat() throws InterruptedException {
        clientFactory.setHeartBeat(true);
        clientFactory.setHeartBeatIntervalMs(300);
        InetSocketAddress address = IpUtils.toInetSocketAddress("localhost:11111");
        clientFactory.getClient(address);
        Thread.sleep(1000);
        ConcurrentHashMap<InetSocketAddress, CopyOnWriteArrayList<Client>> values = clientFactory.getKeepAliveClient();
        assertEquals(1, values.size());
        Client client = values.get(address).get(0);
        assertTrue(client.isConnected());
    }

    @Test
    public void test_keepAlive() throws InterruptedException {
        clientFactory.setHeartBeat(true);
        clientFactory.setHeartBeatIntervalMs(300);
        clientFactory.setKeepAliveMs(1000);
        InetSocketAddress address = IpUtils.toInetSocketAddress("localhost:11111");
        clientFactory.getClient(address);
        Thread.sleep(1000);
        ConcurrentHashMap<InetSocketAddress, CopyOnWriteArrayList<Client>> values = clientFactory.getKeepAliveClient();
        assertEquals(1, values.size());
        Client client = values.get(address).get(0);
        assertTrue(client.isConnected());
    }

    @Test
    public void test_keepAlive_time_out_close() throws InterruptedException {
        clientFactory.setHeartBeat(true);
        clientFactory.setHeartBeatIntervalMs(300);
        clientFactory.setKeepAliveMs(200);
        InetSocketAddress address = IpUtils.toInetSocketAddress("localhost:11111");
        clientFactory.getClient(address);
        Thread.sleep(1000);
        ConcurrentHashMap<InetSocketAddress, CopyOnWriteArrayList<Client>> values = clientFactory.getKeepAliveClient();
        assertEquals(0, values.get(address).size());
    }
}
