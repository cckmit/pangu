package com.pangu.framework.socket.handler;

import com.pangu.framework.protocol.annotation.Transable;
import com.pangu.framework.socket.anno.*;
import com.pangu.framework.socket.client.Client;
import com.pangu.framework.socket.client.ClientFactory;
import com.pangu.framework.socket.client.ClientFactoryBuilder;
import com.pangu.framework.socket.core.CoderType;
import com.pangu.framework.socket.server.SocketServer;
import com.pangu.framework.socket.server.SocketServerBuilder;
import lombok.Data;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class SessionManagerTest {
    private static ClientFactory clientFactory;
    private static SocketServer socketServer;
    static AtomicInteger count = new AtomicInteger();
    private SessionManager sessionManager;

    @Before
    public void init() {
        SocketServerBuilder builder = new SocketServerBuilder();
        socketServer = builder.port(11111).build();
        socketServer.start();
        Dispatcher dispatcher = socketServer.getDispatcher();
        dispatcher.setDefaultCoder(CoderType.JSON);
        sessionManager = socketServer.getSessionManager();
        FacadeImpl facade = new FacadeImpl(sessionManager);
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
    public void test_request_session_bind() {
        Client connect = clientFactory.connect();
        connect.getProxy(Facade.class).login(null, 123);
        assertEquals(1, sessionManager.getAllIdentitySession().size());
        connect.close();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(0, sessionManager.getAllIdentitySession().size());
    }

    @Test
    public void test_push_identity() {
        Client connect = clientFactory.connect();
        connect.getProxy(Facade.class).login(null, 123);
        sessionManager.getPushProxy(FacadePush.class).close(123, new PlayerVo());
        sessionManager.getPushProxy(FacadePush.class).close(new int[]{123}, new PlayerVo());
        sessionManager.getPushProxy(FacadePush.class).close(Collections.singleton(123), new PlayerVo());
        sessionManager.getPushProxy(FacadePush.class).close(new PlayerVo());
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test_kick() throws InterruptedException {
        Client connect = clientFactory.connect();
        Thread.sleep(400);
        assertEquals(1, sessionManager.connectAmount());
        assertEquals(1, sessionManager.anonymousAmount());
        int id = 123;
        connect.getProxy(Facade.class).login(null, id);
        assertEquals(1, sessionManager.connectAmount());
        assertEquals(1, sessionManager.identityAmount());
        sessionManager.kick(id);
        Thread.sleep(500);
        assertEquals(0, sessionManager.connectAmount());
        assertEquals(0, sessionManager.identityAmount());
    }

    @Test
    public void test_session_re_bind() throws InterruptedException {
        Client connect = clientFactory.connect();
        Client connect2 = clientFactory.connect();
        Thread.sleep(100);
        assertEquals(2, sessionManager.connectAmount());
        assertEquals(2, sessionManager.anonymousAmount());
        int id = 123;

        connect.getProxy(Facade.class).login(null, id);

        connect2.getProxy(Facade.class).login(null, id);

        assertEquals(1, sessionManager.connectAmount());
        assertEquals(1, sessionManager.identityAmount());
        Thread.sleep(100);

        assertEquals(1, sessionManager.connectAmount());
        assertEquals(1, sessionManager.identityAmount());
    }

    @SocketModule(1)
    private interface Facade {
        @SocketCommand(2)
        PlayerVo login(Session session, @InBody("hello") int value);
    }

    @SocketModule(1)
    private interface FacadePush {

        @SocketCommand((-1))
        void close(@Identity int identity, @InBody("playerVo") PlayerVo playerVo);

        @SocketCommand((-2))
        void close(@Identity int[] identity, @InBody("playerVo") PlayerVo playerVo);

        @SocketCommand((-3))
        void close(@Identity Collection<Integer> identity, @InBody("playerVo") PlayerVo playerVo);

        @SocketCommand((-3))
        @PushAllIdentityClient
        void close(@InBody("playerVo") PlayerVo playerVo);
    }

    public static class FacadeImpl implements Facade {

        private final SessionManager sessionManager;

        public FacadeImpl(SessionManager sessionManager) {
            this.sessionManager = sessionManager;
        }

        @Override
        public PlayerVo login(Session session, int value) {
            PlayerVo playerVo = new PlayerVo();
            playerVo.name = "hello:" + value;
            Session preSession = sessionManager.getIdentity(value);
            if (preSession != null) {
                sessionManager.getPushProxy(FacadePush.class).close(value, new PlayerVo());
                sessionManager.kick(value);
            }
            sessionManager.bind(session, (long) value);
            return playerVo;
        }
    }

    @Transable
    @Data
    public static class PlayerVo {
        private String name;

    }

}