package com.pangu.gateway.server;

import com.pangu.core.anno.ServiceGate;
import com.pangu.core.common.Constants;
import com.pangu.core.common.InstanceDetails;
import com.pangu.core.common.ServerInfo;
import com.pangu.core.config.ZookeeperConfig;
import com.pangu.framework.utils.os.NetUtils;
import com.pangu.gateway.config.GatewayConfig;
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

    private final AtomicBoolean running = new AtomicBoolean();
    private CuratorFramework framework;
    private ServiceInstance<InstanceDetails> serviceInstance;
    private ServiceDiscovery<InstanceDetails> serviceDiscovery;
    private ServiceCache<InstanceDetails> serverCache;
    private List<ServerInfo> logicServers;

    public GatewayServerManager(GatewayConfig logicConfig) {
        this.logicConfig = logicConfig;
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

        try {
            registerServer();
            initDiscovery();
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
        String id = logicConfig.getZookeeper().getServerId();

        ServiceInstanceBuilder<InstanceDetails> builder = ServiceInstance.<InstanceDetails>builder()
                .id(id)
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

    private void initDiscovery() throws Exception {
        serverCache = serviceDiscovery
                .serviceCacheBuilder()
                .name(Constants.LOGIC_SERVICE_NAME)
                .build();
        serverCache.start();

        initServerService(serverCache);

        log.debug("刷新逻辑服务器列表[{}]", logicServers);
        serverCache.addListener(new ServiceCacheListener() {
            @Override
            public void cacheChanged() {
                initServerService(serverCache);
            }

            @Override
            public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
                log.debug("Zookeeper状态改变[{}]", connectionState);
            }
        });
    }

    private void initServerService(ServiceCache<InstanceDetails> cache) {
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

    @Override
    public void stop() {
        boolean set = running.compareAndSet(true, false);
        if (!set) {
            return;
        }
        if (serverCache != null) {
            CloseableUtils.closeQuietly(serverCache);
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
}
