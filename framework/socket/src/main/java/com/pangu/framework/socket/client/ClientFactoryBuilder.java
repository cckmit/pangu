package com.pangu.framework.socket.client;

import com.pangu.framework.socket.handler.SslConfig;
import com.pangu.framework.utils.thread.NamedThreadFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.ssl.SslContext;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ClientFactoryBuilder {
    // 读缓冲
    private Integer readBuffSize = 8192;
    // 写缓冲
    private Integer writeBuffSize = 8192;

    // 连接超时时间
    private int connectTimeout = 5000;
    // 同步请求超时
    private int readTimeout = 3000;

    // 默认地址
    private InetSocketAddress address;

    private String workThreadName;
    private int workThreadAmount;
    private EventLoopGroup workEventLoop;

    private SslConfig sslConfig;

    private boolean heartBeat;

    private int heartBeatIntervalMs;

    private int keepAliveMs;

    private final Map<String, ChannelHandler> filters = new LinkedHashMap<>();

    public ClientFactoryBuilder readBuffSize(int readBuffSize) {
        this.readBuffSize = readBuffSize;
        return this;
    }

    public ClientFactoryBuilder writeBuffSize(int writeBuffSize) {
        this.writeBuffSize = writeBuffSize;
        return this;
    }

    public ClientFactoryBuilder address(String address) {
        if (StringUtils.isBlank(address)) {
            throw new IllegalArgumentException("服务端绑定地址端口不可为空");
        }
        address = StringUtils.deleteWhitespace(address);
        int splitIndex = address.lastIndexOf(":");
        if (splitIndex < 0) {
            this.address = new InetSocketAddress(Integer.parseInt(address));
            return this;
        }

        String portStr = address.substring(splitIndex + 1);
        int port = Integer.parseInt(portStr);
        if (splitIndex == 0) {
            return this;
        }
        String ip = address.substring(0, splitIndex);

        this.address = new InetSocketAddress(ip, port);
        return this;
    }

    public ClientFactoryBuilder workThreadName(String workThreadName) {
        this.workThreadName = workThreadName;
        return this;
    }

    public ClientFactoryBuilder workEventLoop(EventLoopGroup workEventLoop) {
        this.workEventLoop = workEventLoop;
        return this;
    }

    public ClientFactoryBuilder work(int workThreadAmount) {
        this.workThreadAmount = workThreadAmount;
        return this;
    }

    public ClientFactoryBuilder work(int workThreadAmount, String workThreadName) {
        this.workThreadAmount = workThreadAmount;
        this.workThreadName = workThreadName;
        return this;
    }

    public ClientFactoryBuilder ssl(SslConfig sslConfig) {
        this.sslConfig = sslConfig;
        return this;
    }

    public ClientFactoryBuilder heartBeat(boolean heartBeat) {
        this.heartBeat = heartBeat;
        return this;
    }

    public ClientFactoryBuilder heartBeatIntervalMs(int heartBeatIntervalMs) {
        this.heartBeatIntervalMs = heartBeatIntervalMs;
        return this;
    }

    public ClientFactoryBuilder keepAliveMs(int keepAliveMs) {
        this.keepAliveMs = keepAliveMs;
        return this;
    }

    public ClientFactoryBuilder connectTimeout(int timeout) {
        this.connectTimeout = timeout;
        return this;
    }

    public ClientFactoryBuilder readTimeout(int timeout) {
        this.readTimeout = timeout;
        return this;
    }

    public ClientFactory build() {
        ClientFactory clientFactory = new ClientFactory();

        Map<ChannelOption<?>, Object> connectOptions = buildConnectOptions();
        clientFactory.setConnectOption(connectOptions);

        EventLoopGroup work = buildWorkEventLoop();
        clientFactory.setWorkerGroup(work);

        clientFactory.setReadTimeout(readTimeout);
        clientFactory.setDefaultAddress(address);

        if (sslConfig != null) {
            SslContext sslContext = sslConfig.createForClient();
            clientFactory.setSslContext(sslContext);
        }
        clientFactory.setFilters(filters);

        clientFactory.setHeartBeat(heartBeat);
        clientFactory.setHeartBeatIntervalMs(Math.max(5000, heartBeatIntervalMs));

        if (keepAliveMs > 0) {
            if (!heartBeat) {
                clientFactory.setHeartBeat(true);
            }
            clientFactory.setKeepAliveMs(keepAliveMs);
        }

        return clientFactory;
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
            workName = "客户端NIO线程";
        }
        return new NioEventLoopGroup(workThread, new NamedThreadFactory(workName));
    }

    private Map<ChannelOption<?>, Object> buildConnectOptions() {
        Map<ChannelOption<?>, Object> values = new HashMap<>(3);
        values.put(ChannelOption.SO_RCVBUF, readBuffSize);
        values.put(ChannelOption.SO_SNDBUF, writeBuffSize);
        values.put(ChannelOption.ALLOW_HALF_CLOSURE, false);
        values.put(ChannelOption.TCP_NODELAY, true);
        values.put(ChannelOption.SO_REUSEADDR, true);
        values.put(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
        return values;
    }
}
