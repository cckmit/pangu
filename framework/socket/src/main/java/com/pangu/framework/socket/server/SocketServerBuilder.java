package com.pangu.framework.socket.server;

import com.pangu.framework.socket.handler.param.Coder;
import com.pangu.framework.socket.filter.SocketFilter;
import com.pangu.framework.socket.handler.Dispatcher;
import com.pangu.framework.socket.handler.SessionManager;
import com.pangu.framework.socket.handler.SslConfig;
import com.pangu.framework.utils.thread.NamedThreadFactory;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.util.*;

@Getter
@Slf4j
public class SocketServerBuilder {
    // 读缓冲
    private Integer readBuffSize = 2048;
    // 写缓冲
    private Integer writeBuffSize = 2048;
    // 最大连接数
    private Integer backlog = 256;

    // 绑定地址 [地址1],[地址2]
    private String[] ips;

    private int port = 11111;

    private String bossThreadName;
    private EventLoopGroup bossEventLoop;

    private String workThreadName;
    private int workThreadAmount;
    private EventLoopGroup workEventLoop;

    private boolean manageUseThread;

    private SslConfig sslConfig;

    private Class<? extends ServerSocketChannel> bootstrapClass = NioServerSocketChannel.class;

    private final List<SocketFilter> filters = new ArrayList<>();

    private Dispatcher dispatcher;

    private SessionManager sessionManager;

    public SocketServerBuilder readBuffSize(int readBuffSize) {
        this.readBuffSize = readBuffSize;
        return this;
    }

    public SocketServerBuilder writeBuffSize(int writeBuffSize) {
        this.writeBuffSize = writeBuffSize;
        return this;
    }

    public SocketServerBuilder backlog(int backlog) {
        this.backlog = backlog;
        return this;
    }

    public SocketServerBuilder port(int port) {
        this.port = port;
        return this;
    }

    public SocketServerBuilder address(String address) {
        if (StringUtils.isBlank(address)) {
            throw new IllegalArgumentException("服务端绑定地址端口不可为空");
        }
        address = StringUtils.deleteWhitespace(address);
        int splitIndex = address.lastIndexOf(":");
        if (splitIndex < 0) {
            this.port = Integer.parseInt(address);
            return this;
        }

        String port = address.substring(splitIndex + 1);
        this.port = Integer.parseInt(port);
        if (splitIndex == 0) {
            return this;
        }
        String ipStr = address.substring(0, splitIndex);
        String[] split = ipStr.split(",");
        List<String> ips = new ArrayList<>(split.length);
        for (String ip : split) {
            if (StringUtils.isBlank(ip)) {
                continue;
            }
            ips.add(ip);
        }
        this.ips = ips.toArray(new String[0]);
        return this;
    }

    public SocketServerBuilder bossThreadName(String bossThreadName) {
        this.bossThreadName = bossThreadName;
        return this;
    }

    public SocketServerBuilder bossEventLoop(EventLoopGroup eventLoopGroup) {
        this.bossEventLoop = eventLoopGroup;
        return this;
    }

    public SocketServerBuilder workThreadName(String workThreadName) {
        this.workThreadName = workThreadName;
        return this;
    }

    public SocketServerBuilder workEventLoop(EventLoopGroup workEventLoop) {
        this.workEventLoop = workEventLoop;
        return this;
    }

    public SocketServerBuilder work(int workThreadAmount) {
        this.workThreadAmount = workThreadAmount;
        return this;
    }

    public SocketServerBuilder work(int workThreadAmount, String workThreadName) {
        this.workThreadAmount = workThreadAmount;
        this.workThreadName = workThreadName;
        return this;
    }

    public SocketServerBuilder bootstrapClass(Class<? extends ServerSocketChannel> bootstrapClass) {
        this.bootstrapClass = bootstrapClass;
        return this;
    }

    public SocketServerBuilder addFilter(SocketFilter filter) {
        filters.add(filter);
        return this;
    }

    public SocketServerBuilder filters(Collection<SocketFilter> filters) {
        if (filters == null || filters.isEmpty()) {
            return this;
        }
        this.filters.addAll(filters);
        return this;
    }

    public SocketServerBuilder sslConfig(SslConfig sslConfig) {
        this.sslConfig = sslConfig;
        return this;
    }

    public SocketServerBuilder dispatcher(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
        return this;
    }

    public SocketServerBuilder sessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        return this;
    }

    public SocketServerBuilder manageUseThread(boolean manageUseThread) {
        this.manageUseThread = manageUseThread;
        return this;
    }

    public SocketServer build() {
        SocketServer socketServer = new SocketServer();

        socketServer.setAcceptor(bootstrapClass);

        Map<ChannelOption<?>, ?> acceptOptions = buildAcceptOptions();
        socketServer.setAcceptOption(acceptOptions);

        Map<ChannelOption<?>, ?> connectOptions = buildConnectOptions();
        socketServer.setConnectOption(connectOptions);

        List<SocketFilter> validFilters = validFilter(filters);
        socketServer.setFilters(validFilters);

        EventLoopGroup boss = buildBossEventLoop();
        socketServer.setBossGroup(boss);

        EventLoopGroup work = buildWorkEventLoop();
        socketServer.setWorkerGroup(work);

        InetSocketAddress[] bindAddresses = buildBindAddresses();
        socketServer.setBindAddresses(bindAddresses);

        if (sslConfig != null) {
            SslContext sslContext = sslConfig.createForServer();
            socketServer.setSslContext(sslContext);
        }
        if (dispatcher == null) {
            dispatcher = new Dispatcher();
            dispatcher.setManageUseThread(manageUseThread);
            dispatcher.start();
        }
        socketServer.setDispatcher(dispatcher);
        if (sessionManager == null) {
            Coder coder = dispatcher.getDefaultCoder();
            sessionManager = new SessionManager(dispatcher);
        }
        socketServer.setSessionManager(sessionManager);

        return socketServer;
    }

    private List<SocketFilter> validFilter(List<SocketFilter> filters) {
        if (filters == null || filters.isEmpty()) {
            return Collections.emptyList();
        }
        if (filters.size() == 1) {
            return filters;
        }
        Set<String> exists = new HashSet<>(filters.size());
        List<SocketFilter> valid = new ArrayList<>(filters.size());
        for (SocketFilter socketFilter : filters) {
            String name = socketFilter.getName();
            if (!exists.add(name)) {
                log.warn("重复的Filter[{}],[{}]", name, socketFilter);
                continue;
            }
            valid.add(socketFilter);
        }
        return valid;
    }

    private InetSocketAddress[] buildBindAddresses() {

        if (ips == null || ips.length <= 0) {
            return new InetSocketAddress[]{new InetSocketAddress(port)};
        }
        InetSocketAddress[] addresses = new InetSocketAddress[ips.length];
        for (int i = 0; i < ips.length; ++i) {
            String ip = ips[i];
            addresses[i] = new InetSocketAddress(ip, port);
        }
        return addresses;
    }

    private EventLoopGroup buildWorkEventLoop() {
        if (workEventLoop != null) {
            return workEventLoop;
        }
        int workThread = workThreadAmount;
        if (workThread <= 0) {
            workThread = Runtime.getRuntime().availableProcessors();
        }
        String workName = workThreadName;
        if (workName == null) {
            workName = "服务器NIO线程";
        }
        return new NioEventLoopGroup(workThread, new NamedThreadFactory(workName));
    }

    private EventLoopGroup buildBossEventLoop() {
        if (bossEventLoop != null) {
            return bossEventLoop;
        }
        if (bossThreadName != null) {
            return new NioEventLoopGroup(1, new NamedThreadFactory(bossThreadName));
        }
        return new NioEventLoopGroup(1, new NamedThreadFactory("服务器BOSS线程"));
    }

    private Map<ChannelOption<?>, ?> buildConnectOptions() {
        Map<ChannelOption<?>, Object> values = new HashMap<>(4);
        values.put(ChannelOption.SO_RCVBUF, readBuffSize);
        values.put(ChannelOption.SO_SNDBUF, writeBuffSize);
        values.put(ChannelOption.TCP_NODELAY, true);
        values.put(ChannelOption.ALLOW_HALF_CLOSURE, false);
        return values;
    }

    private Map<ChannelOption<?>, Object> buildAcceptOptions() {
        Map<ChannelOption<?>, Object> values = new HashMap<>(3);
        values.put(ChannelOption.SO_BACKLOG, backlog);
        values.put(ChannelOption.SO_REUSEADDR, true);
        return values;
    }
}

