package com.pangu.gateway.server;

import com.pangu.core.anno.ServiceGate;
import com.pangu.core.common.Constants;
import com.pangu.core.common.InstanceDetails;
import com.pangu.core.common.ServerInfo;
import com.pangu.core.config.ZookeeperConfig;
import com.pangu.core.db.facade.DbFacade;
import com.pangu.framework.socket.client.Client;
import com.pangu.framework.socket.client.ClientFactory;
import com.pangu.framework.socket.handler.DefaultDispatcher;
import com.pangu.framework.socket.handler.Dispatcher;
import com.pangu.framework.socket.handler.Session;
import com.pangu.framework.socket.handler.SessionManager;
import com.pangu.framework.socket.handler.session.IdentitySessionCloseListener;
import com.pangu.framework.socket.handler.session.IdentitySessionListener;
import com.pangu.framework.socket.server.SocketServer;
import com.pangu.framework.utils.os.NetUtils;
import com.pangu.gateway.config.GatewayConfig;
import com.pangu.gateway.rout.RoutProcessor;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.RetryForever;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.*;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.details.ServiceCacheListener;
import org.springframework.context.Lifecycle;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@ServiceGate
@Slf4j
public class GatewayServerManager implements Lifecycle {

    private final GatewayConfig logicConfig;

    private final SocketServer socketServer;
    private final ClientFactory clientFactory;

    private final RoutProcessor routProcessor;

    private final AtomicBoolean running = new AtomicBoolean();
    private CuratorFramework framework;
    private ServiceInstance<InstanceDetails> serviceInstance;
    private ServiceDiscovery<InstanceDetails> serviceDiscovery;
    private ServiceCache<InstanceDetails> logicServerCache;
    private ServiceCache<InstanceDetails> dbServerCache;
    @Getter
    private List<ServerInfo> logicServers;
    @Getter
    private List<ServerInfo> dbServers;

    public GatewayServerManager(GatewayConfig logicConfig,
                                SocketServer socketServer,
                                ClientFactory clientFactory,
                                RoutProcessor routProcessor) {
        this.logicConfig = logicConfig;
        this.socketServer = socketServer;
        this.clientFactory = clientFactory;
        this.routProcessor = routProcessor;
        SessionManager sessionManager = this.socketServer.getSessionManager();
        sessionManager.getIdGenerator().setServerId(logicConfig.getZookeeper().getServerId());
        GateSessionChangeListener listener = new GateSessionChangeListener();
        sessionManager.addListener((IdentitySessionListener) listener);
        sessionManager.addListener((IdentitySessionCloseListener) listener);
    }

    @Override
    public void start() {
        boolean set = running.compareAndSet(false, true);
        if (!set) {
            return;
        }
        ZookeeperConfig zookeeper = logicConfig.getZookeeper();
        framework = CuratorFrameworkFactory.builder()
                .connectString(zookeeper.getAddr())
                .sessionTimeoutMs(20_000)
                .connectionTimeoutMs(10_000)
                .retryPolicy(new RetryForever(3000))
                .build();
        framework.start();

        routProcessor.init(this);

        Dispatcher dispatcher = socketServer.getDispatcher();
        if (dispatcher instanceof DefaultDispatcher) {
            ((DefaultDispatcher) dispatcher).setMessageProcessor(routProcessor);
        }
        socketServer.start();
        try {
            registerServer();
            initLogicDiscovery();
            initDBDiscovery();
        } catch (Exception e) {
            log.warn("注册Logic服务异常", e);
        }
    }

    private void registerServer() throws Exception {
        String address = logicConfig.getSocket().getAddress();
        String[] split = address.trim().split(":");
        if (split.length == 0) {
            throw new IllegalStateException("服务器配置 server.address 配置为空，配置格式: 内网IP:端口，如192.168.11.88:8001");
        }
        String ip = "";
        if (split.length <= 1) {
            InetAddress localAddress = NetUtils.getLocalAddress();
            ip = localAddress.getHostAddress();
        }
        int id = logicConfig.getZookeeper().getServerId();

        ServiceInstanceBuilder<InstanceDetails> builder = ServiceInstance.<InstanceDetails>builder()
                .id(String.valueOf(id))
                .name(Constants.GATEWAY_SERVICE_NAME)
                .address(ip)
                .port(Integer.parseInt(split[1]))
                .payload(new InstanceDetails());

        serviceInstance = builder.build();

        JsonInstanceSerializer<InstanceDetails> serializer = new JsonInstanceSerializer<>(InstanceDetails.class);
        ZookeeperConfig zookeeper = logicConfig.getZookeeper();
        serviceDiscovery = ServiceDiscoveryBuilder.builder(InstanceDetails.class)
                .client(framework)
                .basePath(zookeeper.getRootPath())
                .serializer(serializer)
                .thisInstance(serviceInstance)
                .build();
        serviceDiscovery.start();
    }

    private void initLogicDiscovery() throws Exception {
        logicServerCache = serviceDiscovery
                .serviceCacheBuilder()
                .name(Constants.LOGIC_SERVICE_NAME)
                .build();
        logicServerCache.start();

        initLogicServerService(logicServerCache);

        log.debug("刷新逻辑服务器列表[{}]", logicServers);
        logicServerCache.addListener(new ServiceCacheListener() {
            @Override
            public void cacheChanged() {
                initLogicServerService(logicServerCache);
            }

            @Override
            public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
                log.debug("Zookeeper状态改变[{}]", connectionState);
            }
        });
    }

    private void initDBDiscovery() throws Exception {
        dbServerCache = serviceDiscovery
                .serviceCacheBuilder()
                .name(Constants.DB_SERVICE_NAME)
                .build();
        dbServerCache.start();

        initDBServerService(dbServerCache);

        log.debug("刷新数据服务器列表[{}]", dbServers);
        dbServerCache.addListener(new ServiceCacheListener() {
            @Override
            public void cacheChanged() {
                initDBServerService(dbServerCache);
            }

            @Override
            public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
                log.debug("Zookeeper状态改变[{}]", connectionState);
            }
        });
    }

    private void initLogicServerService(ServiceCache<InstanceDetails> cache) {
        List<ServiceInstance<InstanceDetails>> instances = cache.getInstances();
        List<ServerInfo> servers = new ArrayList<>();

        for (ServiceInstance<InstanceDetails> instance : instances) {
            InstanceDetails payload = instance.getPayload();
            ServerInfo serverInfo = new ServerInfo(instance.getId(), instance.getAddress(), instance.getPort(), payload.getAddressForClient(), payload);
            servers.add(serverInfo);
        }
        logicServers = servers;
        log.debug("当前逻辑服列表[{}]", servers);
    }

    private void initDBServerService(ServiceCache<InstanceDetails> cache) {
        List<ServiceInstance<InstanceDetails>> instances = cache.getInstances();
        List<ServerInfo> servers = new ArrayList<>();

        for (ServiceInstance<InstanceDetails> instance : instances) {
            InstanceDetails payload = instance.getPayload();
            ServerInfo serverInfo = new ServerInfo(instance.getId(), instance.getAddress(), instance.getPort(), payload.getAddressForClient(), payload);
            servers.add(serverInfo);
        }
        dbServers = servers;
        log.debug("当前DB服列表[{}]", servers);
    }

    @Override
    public void stop() {
        boolean set = running.compareAndSet(true, false);
        if (!set) {
            return;
        }
        if (logicServerCache != null) {
            CloseableUtils.closeQuietly(logicServerCache);
        }
        if (dbServerCache != null) {
            CloseableUtils.closeQuietly(dbServerCache);
        }
        try {
            serviceDiscovery.unregisterService(serviceInstance);
        } catch (Exception e) {
            log.debug("取消注册服务[{}]", Constants.LOGIC_SERVICE_NAME, e);
        }
        CloseableUtils.closeQuietly(serviceDiscovery);
        if (framework != null) {
            CloseableUtils.closeQuietly(framework);
        }
        log.debug("服务器[{}]取消注册进入服务器", Constants.LOGIC_SERVICE_NAME);
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    public class GateSessionChangeListener implements IdentitySessionCloseListener, IdentitySessionListener {

        @Override
        public void close(long identity, Session session, Channel channel) {
            if (dbServers == null) {
                return;
            }
            for (ServerInfo serverInfo : dbServers) {
                try {
                    Client client = clientFactory.getClient(serverInfo.getAddress());
                    DbFacade dbFacade = client.getProxy(DbFacade.class);
                    dbFacade.offline(session.getId(), identity);
                } catch (Throwable throwable) {
                    log.warn("[{}][{}]同步离线数据失败[{}]", session.getId(), identity, serverInfo.getAddress(), throwable);
                }
            }
        }

        @Override
        public void identity(long identity, Session session) {
            if (dbServers == null) {
                return;
            }

            for (ServerInfo serverInfo : dbServers) {
                try {
                    Client client = clientFactory.getClient(serverInfo.getAddress());
                    DbFacade dbFacade = client.getProxy(DbFacade.class);
                    dbFacade.online(session.getId(), identity);
                    // todo 当存在替换的sessionId时，必须通知对应网关将tcp连接关闭
                } catch (Throwable throwable) {
                    log.warn("[{}][{}]同步离线数据失败[{}]", session.getId(), identity, serverInfo.getAddress(), throwable);
                }
            }
        }
    }
}
