package com.pangu.stress.server;

import com.pangu.core.anno.ComponentStress;
import com.pangu.core.common.Constants;
import com.pangu.core.common.InstanceDetails;
import com.pangu.core.common.ServerInfo;
import com.pangu.core.config.ZookeeperConfig;
import com.pangu.stress.config.StressConfig;
import com.pangu.stress.module.StressManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.RetryForever;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.ServiceCache;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.details.ServiceCacheListener;
import org.springframework.context.Lifecycle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@ComponentStress
@Slf4j
public class StressServerManager implements Lifecycle {

    private final AtomicBoolean running = new AtomicBoolean();
    private CuratorFramework framework;
    private ServiceDiscovery<InstanceDetails> serviceDiscovery;
    private ServiceCache<InstanceDetails> serverCache;
    @Getter
    private List<ServerInfo> gateServers;

    private final StressConfig stressConfig;

    private final StressManager stressManager;

    public StressServerManager(StressConfig stressConfig, StressManager stressManager) {
        this.stressConfig = stressConfig;
        this.stressManager = stressManager;
    }

    @Override
    public void start() {
        boolean set = running.compareAndSet(false, true);
        if (!set) {
            return;
        }
        ZookeeperConfig zookeeper = stressConfig.getZookeeper();
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
            log.warn("压力服务异常", e);
        }
    }

    private void registerServer() throws Exception {
        JsonInstanceSerializer<InstanceDetails> serializer = new JsonInstanceSerializer<>(InstanceDetails.class);
        ZookeeperConfig zookeeper = stressConfig.getZookeeper();
        serviceDiscovery = ServiceDiscoveryBuilder.builder(InstanceDetails.class)
                .client(framework)
                .basePath(zookeeper.getRootPath())
                .serializer(serializer)
                .build();
        serviceDiscovery.start();
    }

    private void initDiscovery() throws Exception {
        serverCache = serviceDiscovery
                .serviceCacheBuilder()
                .name(Constants.GATEWAY_SERVICE_NAME)
                .build();
        serverCache.start();

        initServerService(serverCache);

        log.debug("首次刷新数据服务器列表[{}]", gateServers);
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
        gateServers = servers;
        try {
            stressManager.serverChange(gateServers);
        } catch (Throwable thr) {
            log.info("压力逻辑调度异常", thr);
        }
        log.debug("当前数据服列表[{}]", servers);
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
