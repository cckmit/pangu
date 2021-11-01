package com.pangu.framework.socket.client;

import com.pangu.framework.socket.anno.InBody;
import com.pangu.framework.socket.anno.SocketCommand;
import com.pangu.framework.socket.anno.SocketModule;
import com.pangu.framework.socket.handler.Dispatcher;
import com.pangu.framework.socket.server.SocketServer;
import com.pangu.framework.socket.server.SocketServerBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientMultiConnectTPSTest {

    public static void main(String[] args) {
        init();

        int connect = 1000;
        List<Client> clients = new ArrayList<>();
        long start = System.currentTimeMillis();
        for (int i = 0; i < connect; ++i) {
            Client client = clientFactory.connect();
            clients.add(client);
        }
        System.out.println("创建[" + connect + "]个连接总耗时:" + (System.currentTimeMillis() - start) + " ms");

        int thread = 20;

        for (int i = 0; i < thread; ++i) {
            int thIndex = i;
            Thread tt = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    for (int j = 0; j < connect; ++j) {
                        for (Client client : clients) {
                            String res = client.getProxy(Facade.class).hello(thIndex + ":" + j);
                            count.incrementAndGet();
                        }
                    }
                }
            });
            tt.setDaemon(true);
            tt.start();
        }
        System.out.println("client发送完毕");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    @SocketModule(1)
    private interface Facade {
        @SocketCommand(2)
        String hello(@InBody String value);
    }

    static class FacadeImpl implements Facade {

        @Override
        public String hello(String value) {
            return "hello:" + value;
        }
    }
}
