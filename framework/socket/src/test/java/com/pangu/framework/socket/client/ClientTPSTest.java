package com.pangu.framework.socket.client;

import com.pangu.framework.socket.anno.InBody;
import com.pangu.framework.socket.anno.SocketCommand;
import com.pangu.framework.socket.anno.SocketModule;
import com.pangu.framework.socket.handler.DefaultDispatcher;
import com.pangu.framework.socket.handler.Dispatcher;
import com.pangu.framework.socket.server.SocketServer;
import com.pangu.framework.socket.server.SocketServerBuilder;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientTPSTest {

    public static void main(String[] args) {
        init();
        int thread = 20;
        int times = 100_0000;

        for (int i = 0; i < thread; ++i) {
            int thIndex = i;
            Client client = clientFactory.connect();
            Thread tt = new Thread(() -> {
                for (int j = 0; j < times; ++j) {
                    Facade proxy = client.getProxy(Facade.class);
                    String res = proxy.hello(thIndex * times + j);
                    count.incrementAndGet();
                }
            });
            tt.setDaemon(true);
            tt.start();
        }
        System.out.println("client发送完毕，共：" + (thread * times));
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        tearDown();
    }

    private static ClientFactory clientFactory;
    private static SocketServer socketServer;
    static AtomicInteger count = new AtomicInteger();

    public static void init() {
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
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int c = count.getAndSet(0);
                System.out.println(c);
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public static void tearDown() {
        socketServer.stop();
        clientFactory.stop();
    }

    @SocketModule(1)
    private interface Facade {
        @SocketCommand(2)
        String hello(@InBody int value);
    }

    static class FacadeImpl implements Facade {

        @Override
        public String hello(int value) {
            return "hello:" + value;
        }
    }
}