package com.pangu.framework.socket.client;

import com.pangu.framework.protocol.annotation.Transable;
import com.pangu.framework.socket.anno.InBody;
import com.pangu.framework.socket.anno.SocketCommand;
import com.pangu.framework.socket.anno.SocketModule;
import com.pangu.framework.socket.core.Command;
import com.pangu.framework.socket.exception.SocketException;
import com.pangu.framework.socket.handler.Dispatcher;
import com.pangu.framework.socket.handler.Session;
import com.pangu.framework.socket.server.SocketServer;
import com.pangu.framework.socket.server.SocketServerBuilder;
import com.pangu.framework.utils.ManagedException;
import com.pangu.framework.utils.model.Result;
import lombok.Data;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class SimpleClientTest {
    static AtomicInteger count = new AtomicInteger();
    private static ClientFactory clientFactory;
    private static SocketServer socketServer;

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

    @Test
    public void test_simple() {
        Client client = clientFactory.connect();
        Facade proxy = client.getProxy(Facade.class);
        int len = 12 * 1024 * 1024;
        StringBuilder builder = new StringBuilder(len);
        for (int i = 0; i < len; ++i) {
            builder.append(1);
        }
        String value = builder.toString();
        long start = System.currentTimeMillis();
        PlayerVo hello = proxy.hello(value);
        System.out.println(System.currentTimeMillis() - start);
        assertEquals(hello.getName(), value);
        client.close();
    }

    @Test
    public void test_close_by_remote() {
        Client client = clientFactory.connect();
        Facade proxy = client.getProxy(Facade.class);
        try {
            proxy.close(null);
        } catch (SocketException ignored) {

        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        client.close();
    }

    @Test
    public void test_send_async() {
        Client client = clientFactory.connect();
        Facade proxy = client.getProxy(Facade.class);
        try {
            Result<PlayerVo> res = proxy.helloAsync(1, null);
            PlayerVo playerVo = res.getContent();
            assertEquals(playerVo.name, "ok");
        } catch (SocketException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        client.close();
    }

    @Test
    public void test_send_async_exception() {
        Client client = clientFactory.connect();
        Facade proxy = client.getProxy(Facade.class);
        try {
            proxy.helloAsync(20, null);
        } catch (ManagedException e) {
            assertEquals(-100, e.getCode());
        }
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        client.close();
    }

    @Test
    public void test_send_by_command() throws ExecutionException, InterruptedException {
        Client client = clientFactory.connect();
        Client.registerAndGetMethodDefine(Facade.class);
        CompletableFuture<PlayerVo> hello = client.send(Command.of(2, 1), Collections.singletonMap("hello", 10));
        PlayerVo playerVo = hello.get();
        assertEquals("10", playerVo.name);
    }

    @SocketModule(1)
    private interface Facade {
        @SocketCommand(2)
        PlayerVo hello(@InBody("hello") String value);

        @SocketCommand((3))
        void close(Session session);

        @SocketCommand(4)
        Result<PlayerVo> helloAsync(@InBody("hello") int value, CompletableFuture<Result<PlayerVo>> completableFuture);
    }

    public static class FacadeImpl implements Facade {

        @Override
        public PlayerVo hello(String value) {
            PlayerVo playerVo = new PlayerVo();
            playerVo.name = value;
            return playerVo;
        }

        @Override
        public void close(Session session) {
            session.close();
        }

        @Override
        public Result<PlayerVo> helloAsync(int value, CompletableFuture<Result<PlayerVo>> completableFuture) {
            if (value > 10) {
                completableFuture.completeExceptionally(new ManagedException(-100));
            } else {
                PlayerVo ok = new PlayerVo();
                ok.name = "ok";
                completableFuture.complete(Result.SUCCESS(ok));
            }
            return null;
        }
    }

    @Transable
    @Data
    public static class PlayerVo {
        private String name;

    }
}
