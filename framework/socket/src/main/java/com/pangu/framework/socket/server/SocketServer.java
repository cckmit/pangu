package com.pangu.framework.socket.server;

import com.pangu.framework.socket.filter.SocketFilter;
import com.pangu.framework.socket.handler.DefaultDispatcher;
import com.pangu.framework.socket.handler.Dispatcher;
import com.pangu.framework.socket.handler.SessionManager;
import com.pangu.framework.socket.handler.SyncSupport;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Getter
public class SocketServer implements SocketServerMBean {

    // 运行状态
    private final AtomicBoolean status = new AtomicBoolean(false);
    @Setter
    private Class<? extends ServerSocketChannel> acceptor;
    @Setter
    private List<SocketFilter> filters;
    @Setter
    private EventLoopGroup bossGroup;
    @Setter
    private EventLoopGroup workerGroup;
    @Setter
    private Map<ChannelOption<?>, ?> acceptOption;
    @Setter
    private Map<ChannelOption<?>, ?> connectOption;
    @Setter
    private InetSocketAddress[] bindAddresses;
    @Setter
    private SslContext sslContext;
    @Setter
    private Dispatcher dispatcher;
    @Setter
    private boolean closeThreadGroupWhenShutdown = true;
    @Setter
    private SessionManager sessionManager;
    // 绑定channel
    private Channel[] channels;
    private ObjectName jmxName;


    SocketServer() {
    }

    public synchronized void start() {
        if (!status.compareAndSet(false, true)) {
            return;
        }
        try {
            ServerBootstrap connector = new ServerBootstrap();
            connector.group(bossGroup, workerGroup);
            connector.channel(acceptor);
            setOptions(connector);

            if (sessionManager == null) {
                sessionManager = new SessionManager(dispatcher);
            }

            connector.childHandler(new ServerInitializer(dispatcher, sessionManager, sslContext, filters));

            List<Channel> channels = new ArrayList<>(bindAddresses.length);
            for (InetSocketAddress address : bindAddresses) {
                // 只能绑定一个地址
                connector.localAddress(address);
                Channel channel = connector.bind().sync().channel();
                if (log.isInfoEnabled()) {
                    log.info("绑定服务地址和端口到[{}:{}]", address.getHostString(), address.getPort());
                }
                channels.add(channel);
            }
            this.channels = channels.toArray(new Channel[0]);
            status.set(true);
        } catch (InterruptedException e) {
            log.error("启动异常", e);
        }
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            int singlePort = bindAddresses[0].getPort();
            jmxName = new ObjectName("com.pangu.framework:type=SocketServerMBean(" + singlePort + ")");
            mbs.registerMBean(this, jmxName);
        } catch (Exception e) {
            log.error("注册[common-socket]的JMX管理接口失败", e);
        }
    }

    private void setOptions(ServerBootstrap connector) {
        for (Map.Entry<?, ?> e : acceptOption.entrySet()) {
            //noinspection unchecked
            ChannelOption<Object> key = (ChannelOption<Object>) e.getKey();
            Object value = e.getValue();
            connector.option(key, value);
        }

        for (Map.Entry<?, ?> e : connectOption.entrySet()) {
            //noinspection unchecked
            ChannelOption<Object> key = (ChannelOption<Object>) e.getKey();
            Object value = e.getValue();
            connector.childOption(key, value);
        }
    }

    public synchronized void stop() {
        if (this.jmxName != null) {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            try {
                mbs.unregisterMBean(jmxName);
            } catch (InstanceNotFoundException | MBeanRegistrationException e) {
                log.error("unregister JMX", e);
            }
        }
        dispatcher.shutdown();
        if (channels != null) {
            for (Channel channel : channels) {
                channel.close().awaitUninterruptibly();
                log.info("服务器端口关闭[{}]", channel.localAddress());
            }
        }
        if (closeThreadGroupWhenShutdown) {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
        status.set(false);

        log.info("服务器关闭成功");
    }

    @Override
    public String getBindAddress() {
        StringBuilder builder = new StringBuilder();
        for (InetSocketAddress address : bindAddresses) {
            builder.append(address.toString());
        }
        return builder.toString();
    }

    @Override
    public String getManageQueueSize() {
        ThreadPoolExecutor managedExecutorService = DefaultDispatcher.getManagedExecutorService();
        if (managedExecutorService == null) {
            return "";
        }
        BlockingQueue<Runnable> queue = managedExecutorService.getQueue();
        return String.valueOf(queue.size());
    }

    @Override
    public String getMessageQueueSize() {
        ThreadPoolExecutor[] messagePoolExecutors = DefaultDispatcher.getMessagePoolExecutors();
        StringBuilder builder = new StringBuilder();
        if (messagePoolExecutors != null) {
            for (ThreadPoolExecutor poolExecutor : messagePoolExecutors) {
                BlockingQueue<Runnable> queue = poolExecutor.getQueue();
                int size = queue.size();
                builder.append(size).append(",");
            }
        }
        return builder.toString();
    }

    @Override
    public String getSyncQueue() {
        SyncSupport syncSupport = DefaultDispatcher.getSyncSupport();
        if (syncSupport == null) {
            return "";
        }
        ConcurrentMap<String, ThreadPoolExecutor> threads = syncSupport.getThreads();
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, ThreadPoolExecutor> entry : threads.entrySet()) {
            String name = entry.getKey();
            ThreadPoolExecutor poolExecutor = entry.getValue();
            BlockingQueue<Runnable> curQueue = poolExecutor.getQueue();
            builder.append(name).append(":").append(curQueue.size()).append(",");
        }
        return builder.toString();
    }
}
