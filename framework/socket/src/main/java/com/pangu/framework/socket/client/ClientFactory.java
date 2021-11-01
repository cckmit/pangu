package com.pangu.framework.socket.client;

import com.pangu.framework.socket.filter.HeartBeatFilter;
import com.pangu.framework.socket.handler.param.Coder;
import com.pangu.framework.socket.handler.param.JsonCoder;
import com.pangu.framework.socket.codec.MessageDecoder;
import com.pangu.framework.socket.codec.MessageEncoder;
import com.pangu.framework.socket.core.Message;
import com.pangu.framework.socket.exception.ExceptionCode;
import com.pangu.framework.socket.exception.SocketException;
import com.pangu.framework.socket.utils.IpUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.Attribute;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLEngine;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ClientFactory {

    @Setter
    private Map<ChannelOption<?>, Object> connectOption;

    @Setter
    private EventLoopGroup workerGroup;

    @Setter
    private InetSocketAddress defaultAddress;

    @Setter
    private SslContext sslContext;

    @Setter
    private Map<String, ChannelHandler> filters;

    @Setter
    private Coder coder = new JsonCoder();

    @Setter
    private boolean heartBeat;

    @Setter
    private int heartBeatIntervalMs = 5000;

    @Setter
    private int keepAliveMs = 300000;

    @Setter
    private int readTimeout = 3000;

    // 单个地址，同时建立连接的数量
    @Setter
    private int domainConnectLimit = 4;

    // 是否正在运行
    private Bootstrap connector;

    // 保持连接的client集合
    private final ConcurrentHashMap<InetSocketAddress, CopyOnWriteArrayList<Client>> keepAliveClient = new ConcurrentHashMap<>();
    private final AtomicBoolean keepAliveThreadRunning = new AtomicBoolean(false);
    private Thread keepAliveThread;

    public synchronized void start() {
        domainConnectLimit = Math.max(1, domainConnectLimit);
        connector = initBootstrap();
        ClientHandler clientHandler = new ClientHandler();
        HeartBeatFilter heartBeatFilter = new HeartBeatFilter();
        connector.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                if (sslContext != null) {
                    ByteBufAllocator alloc = ch.alloc();
                    SSLEngine sslEngine = sslContext.newEngine(alloc);
                    sslEngine.setUseClientMode(true);
                    sslEngine.setNeedClientAuth(false);
                    pipeline.addFirst("ssl", new SslHandler(sslEngine));
                }
                for (Map.Entry<String, ChannelHandler> entry : filters.entrySet()) {
                    String k = entry.getKey();
                    ChannelHandler v = entry.getValue();
                    pipeline.addLast(k, v);
                }
                initCodecFilter(pipeline);
                pipeline.addLast("HEART-BEAT", heartBeatFilter);
                pipeline.addLast("handler", clientHandler);
            }
        });
    }

    protected void initCodecFilter(ChannelPipeline pipeline) {
        pipeline.addLast("decoder", new MessageDecoder());
        pipeline.addLast("encoder", new MessageEncoder());
    }

    private Bootstrap initBootstrap() {
        Bootstrap connector = new Bootstrap();
        connector.group(workerGroup);
        connector.channel(NioSocketChannel.class);

        if (connectOption != null) {
            for (Map.Entry<ChannelOption<?>, ?> entry : connectOption.entrySet()) {
                @SuppressWarnings("unchecked") ChannelOption<Object> k = (ChannelOption<Object>) entry.getKey();
                connector.option(k, entry.getValue());
            }
        }
        return connector;
    }

    public Client connect() {
        return connect(defaultAddress);
    }

    public Client connect(String address) {
        InetSocketAddress inetSocketAddress = IpUtils.toInetSocketAddress(address);
        return connect(inetSocketAddress);
    }

    private Client connect(InetSocketAddress address) {
        Channel channel;
        try {
            ChannelFuture connect = connector.connect(address);
            channel = connect.sync().channel();
        } catch (InterruptedException e) {
            throw new SocketException(ExceptionCode.CONNECT_INTERRUPTED);
        }
        Client client = new Client(channel);
        Attribute<Client> attr = channel.attr(Client.CLIENT_KEY);
        attr.set(client);
        client.setCoder(coder);
        client.setReadTimeout(readTimeout);
        return client;
    }

    public Client getClient() {
        return getClient(defaultAddress);
    }

    public Client getClient(String address) {
        InetSocketAddress inetSocketAddress = IpUtils.toInetSocketAddress(address);
        return getClient(inetSocketAddress);
    }

    public Client getClient(InetSocketAddress address) {
        CopyOnWriteArrayList<Client> clients = keepAliveClient.computeIfAbsent(address, k -> new CopyOnWriteArrayList<>());
        if (heartBeat && keepAliveThreadRunning.compareAndSet(false, true)) {
            startKeepAliveThread();
        }
        long min = Long.MAX_VALUE;
        Client choose = null;
        for (Client client : clients) {
            long concurrent = client.getConcurrent();
            if (concurrent < min) {
                min = concurrent;
                choose = client;
            }
        }
        if (choose == null || min > 0) {
            if (clients.size() < domainConnectLimit) {
                //noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (clients) {
                    if (clients.size() < domainConnectLimit) {
                        Client client = connect(address);
                        clients.add(client);
                        Channel channel = client.getChannel();
                        Attribute<InetSocketAddress> attr = channel.attr(Client.CLIENT_HEART_BEAT);
                        attr.set(address);
                        choose = client;
                    }
                }
            }
            if (choose == null) {
                min = Long.MAX_VALUE;
                for (Client client : clients) {
                    long concurrent = client.getConcurrent();
                    if (concurrent < min) {
                        min = concurrent;
                        choose = client;
                    }
                }
            }
        }
        return choose;
    }

    private void startKeepAliveThread() {
        if (heartBeat && keepAliveThread != null && keepAliveThread.isAlive()) {
            return;
        }
        keepAliveThread = new Thread(this::heartBeat, "ClientFactory keepAlive");
        keepAliveThread.setDaemon(true);
        keepAliveThread.start();
    }

    public void stop() {
        keepAliveThreadRunning.set(false);
        workerGroup.shutdownGracefully();
        stopAllKeepAliveClient();
        if (keepAliveThread != null) {
            keepAliveThread.interrupt();
        }
    }

    private void stopAllKeepAliveClient() {
        for (Map.Entry<InetSocketAddress, CopyOnWriteArrayList<Client>> entry : keepAliveClient.entrySet()) {
            for (Client client : entry.getValue()) {
                if (!client.isConnected()) {
                    continue;
                }
                try {
                    client.close();
                } catch (Exception e) {
                    log.warn("client关闭连接错误", e);
                }
            }
        }
    }

    private void heartBeat() {
        while (keepAliveThreadRunning.get()) {
            try {
                //noinspection BusyWait
                Thread.sleep(heartBeatIntervalMs);
            } catch (InterruptedException e) {
                if (!keepAliveThreadRunning.get()) {
                    break;
                }
            }
            for (Map.Entry<InetSocketAddress, CopyOnWriteArrayList<Client>> entry : keepAliveClient.entrySet()) {
                CopyOnWriteArrayList<Client> clients = entry.getValue();
                for (Client client : clients) {
                    if (!client.isConnected()) {
                        clients.remove(client);
                        continue;
                    }
                    Channel channel = client.getChannel();
                    Attribute<Long> attr = channel.attr(Client.LAST_MESSAGE_TIME);
                    Long lastTime = attr.get();
                    if (lastTime == null) {
                        attr.set(System.currentTimeMillis());
                    } else {
                        if (keepAliveMs > 0 && (System.currentTimeMillis() - lastTime) >= keepAliveMs) {
                            log.debug("client:[{}]keepAlive超时,将被关闭", client);
                            client.close();
                            return;
                        }
                    }
                    try {
                        client.heartBeat();
                    } catch (Exception e) {
                        log.warn("client心跳连接数据包错误", e);
                    }
                }
            }
        }
    }

    ConcurrentHashMap<InetSocketAddress, CopyOnWriteArrayList<Client>> getKeepAliveClient() {
        return keepAliveClient;
    }

    public Coder getCoder() {
        return coder;
    }

    @ChannelHandler.Sharable
    class ClientHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            Message message = (Message) msg;
            Channel channel = ctx.channel();
            Attribute<Client> attr = channel.attr(Client.CLIENT_KEY);
            Client client = attr.get();
            client.receive(message);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            Channel channel = ctx.channel();
            Attribute<Client> attr = channel.attr(Client.CLIENT_KEY);
            Attribute<InetSocketAddress> keepAliveAttr = channel.attr(Client.CLIENT_HEART_BEAT);
            InetSocketAddress address = keepAliveAttr.get();
            Client client = attr.get();
            client.close();
            if (address != null) {
                CopyOnWriteArrayList<Client> clients = keepAliveClient.get(address);
                if (clients != null) {
                    clients.remove(client);
                }
            }
        }
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) throws Exception {
            if (throwable == null) {
                return;
            }
            SocketAddress remoteAddress = ctx.channel().remoteAddress();

            // write to closed socket channel
            if (throwable instanceof SocketException || throwable instanceof DecoderException) {
                log.info("socket层发生未知错误" + throwable.getMessage());
                return;
            }
            if (throwable instanceof IOException) {
                log.debug("socket关闭时仍有数据通信产生异常[{}]", remoteAddress);
                return;
            }

            // 生成错误堆栈信息
            StringBuilder sb = new StringBuilder();
            Throwable ex = throwable;
            while (ex != null) {
                StackTraceElement[] stackTrace = ex.getStackTrace();
                for (StackTraceElement st : stackTrace) {
                    String className = st.getClassName();

                    if (className.startsWith("sun.") || className.startsWith("java.")
                            || className.startsWith("org.springframework")) {
                        continue;
                    }
                    sb.append("\t").append(st.toString()).append("\n");
                    // NETTY底层错误不再打印
                    if (className.startsWith("io.netty.handler")) {
                        break;
                    }
                }

                if (ex != ex.getCause()) {
                    ex = ex.getCause();
                    if (ex != null) {
                        sb.append("CAUSE\n").append(ex.getMessage()).append(ex).append("\n");
                    }

                } else {
                    break;
                }
            }

            log.error("{}\tError: {} - {}\n{}",
                    throwable.getClass().getName(), throwable.getMessage(), remoteAddress, sb.toString());
        }
    }
}
