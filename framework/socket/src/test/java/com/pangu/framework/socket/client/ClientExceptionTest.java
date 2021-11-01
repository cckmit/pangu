package com.pangu.framework.socket.client;

import com.pangu.framework.socket.anno.*;
import com.pangu.framework.socket.core.Command;
import com.pangu.framework.socket.exception.SocketException;
import com.pangu.framework.socket.handler.Dispatcher;
import com.pangu.framework.socket.server.SocketServer;
import com.pangu.framework.socket.server.SocketServerBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.ConnectException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientExceptionTest {
    private static ClientFactory clientFactory;
    private static SocketServer socketServer;
    static AtomicInteger count = new AtomicInteger();

    @Before
    public void init() {
        SocketServerBuilder builder = new SocketServerBuilder();
        socketServer = builder.port(11111).build();
        socketServer.start();
        Dispatcher dispatcher = socketServer.getDispatcher();
        FacadeImpl facade = new FacadeImpl();
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

    @Test(expected = ConnectException.class)
    public void test_connect_exception() {
        clientFactory.getClient("localhost:11");
    }

    @Test(expected = SocketException.class)
    public void test_identity_excetion() {
        Client client = clientFactory.getClient();
        client.getProxy(Facade.class).identity("abc");
    }

    @Test(expected = SocketException.class)
    public void test_managed_excetion() {
        Client client = clientFactory.getClient();
        client.getProxy(Facade.class).manager("abc");
    }

    @Test
    public void test_type_excetion() {
        Client client = clientFactory.getClient();
        Client.registerAndGetMethodDefine(Facade.class);
        CompletableFuture<Object> send = client.send(Command.of(3, 1), Collections.singletonMap("aaa", "def"));
        try {
            send.get(3, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    @SocketModule(1)
    private interface Facade {

        @SocketCommand(1)
        void identity(@Identity String id);

        @SocketCommand(2)
        @Manager
        void manager(String id);

        @SocketCommand(3)
        void codec(@InBody String id);

    }

    public static class FacadeImpl implements Facade {

        @Override
        public void identity(String id) {

        }

        @Override
        public void manager(String id) {

        }

        @Override
        public void codec(String id) {
            System.out.println();
        }
    }
}
