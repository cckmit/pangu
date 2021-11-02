package com.pangu.db.server;

import com.pangu.db.config.DbConfig;
import com.pangu.db.config.ZookeeperConfig;
import com.pangu.framework.utils.os.NetUtils;
import com.pangu.model.anno.ComponentDb;
import com.pangu.model.common.Constants;
import com.pangu.model.common.InstanceDetails;
import com.pangu.model.common.ServerInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.RetryForever;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.*;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.details.ServiceCacheListener;
import org.apache.zookeeper.data.Stat;
import org.springframework.context.Lifecycle;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@ComponentDb
@Slf4j
public class DbServerManager implements Lifecycle {

    private final DbConfig dbConfig;
    private final DbDatabaseManager dbDatabaseManager;

    private final AtomicBoolean running = new AtomicBoolean();
    private CuratorFramework framework;
    private ServiceInstance<InstanceDetails> serviceInstance;
    private ServiceDiscovery<InstanceDetails> serviceDiscovery;
    private ServiceCache<InstanceDetails> serverCache;
    private List<ServerInfo> dbServers;
    private DistributedBarrier barrier;

    public DbServerManager(DbConfig dbConfig, DbDatabaseManager dbDatabaseManager) {
        this.dbConfig = dbConfig;
        this.dbDatabaseManager = dbDatabaseManager;
    }

    @Override
    public void start() {
        boolean set = running.compareAndSet(false, true);
        if (!set) {
            return;
        }
        ZookeeperConfig zookeeper = dbConfig.getZookeeper();
        framework = CuratorFrameworkFactory.builder()
                .connectString(zookeeper.getAddr())
                .sessionTimeoutMs(20_000)
                .connectionTimeoutMs(10_000)
                .retryPolicy(new RetryForever(3000))
                .build();
        framework.start();

        barrier = new DistributedBarrier(framework, Constants.DB_MINIT_STARTUP);
        log.debug("DB等待最少服务启动");
        try {
            barrier.setBarrier();
        } catch (Exception e) {
            log.warn("等待其他数据服启动失败");
        }
        try {
            registerServer();
            initDiscovery();
        } catch (Exception e) {
            log.warn("注册服务异常", e);
        }
        try {
            barrier.waitOnBarrier();
        } catch (Exception e) {
            log.warn("同时启动兼容支持异常", e);
        }

        startLeaderElection();
    }

    private void registerServer() throws Exception {
        String address = dbConfig.getServer().getAddress();
        String[] split = address.trim().split(":");
        if (split.length == 0) {
            throw new IllegalStateException("服务器配置 server.address 配置为空，配置格式: 内网IP:端口，如192.168.11.88:8001");
        }
        String ip = "";
        if (split.length <= 1) {
            InetAddress localAddress = NetUtils.getLocalAddress();
            ip = localAddress.getHostAddress();
        }
        String id = ip + ":" + split[1];

        ServiceInstanceBuilder<InstanceDetails> builder = ServiceInstance.<InstanceDetails>builder()
                .id(id)
                .name(Constants.DB_SERVICE_NAME)
                .address(ip)
                .port(Integer.parseInt(split[1]))
                .payload(new InstanceDetails());

        serviceInstance = builder.build();

        JsonInstanceSerializer<InstanceDetails> serializer = new JsonInstanceSerializer<>(InstanceDetails.class);
        ZookeeperConfig zookeeper = dbConfig.getZookeeper();
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
                .name(Constants.DB_SERVICE_NAME)
                .build();
        serverCache.start();

        initGameServerService(serverCache);

        log.debug("首次刷新数据服务器列表[{}]", dbServers);
        serverCache.addListener(new ServiceCacheListener() {
            @Override
            public void cacheChanged() {
                initGameServerService(serverCache);
            }

            @Override
            public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
                log.debug("Zookeeper状态改变[{}]", connectionState);
            }
        });
    }

    private void initGameServerService(ServiceCache<InstanceDetails> cache) {
        List<ServiceInstance<InstanceDetails>> instances = cache.getInstances();
        List<ServerInfo> servers = new ArrayList<>();

        for (ServiceInstance<InstanceDetails> instance : instances) {
            InstanceDetails payload = instance.getPayload();
            ServerInfo serverInfo = new ServerInfo(instance.getId(), instance.getAddress(), instance.getPort(), payload.getAddressForClient(), payload);
            servers.add(serverInfo);
        }
        dbServers = servers;
        if (servers.size() >= dbConfig.getMinStartUp()) {
            try {
                barrier.removeBarrier();
                barrier = null;
            } catch (Exception e) {
                log.warn("同时启动兼容支持异常", e);
            }
        }
        log.debug("当前数据服列表[{}]", servers);
    }

    private void startLeaderElection() {
        LeaderLatch latch = new LeaderLatch(framework, dbConfig.getZookeeper().getRootPath() + Constants.DB_LEADER_PATH);
        try {
            latch.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Thread thread = new Thread(() -> startLeaderElection0(latch), "DB-leader-job");
        thread.setDaemon(true);
        thread.start();
    }

    private void startLeaderElection0(LeaderLatch latch) {
        while (running.get()) {
            try {
                latch.await();
            } catch (InterruptedException | EOFException e) {
                log.info("DB Leader 选举被中断", e);
            }
            if (latch.hasLeadership()) {
                try {
                    startLeaderJob();
                } catch (Exception e) {
                    log.error("DB Server执行主服逻辑异常", e);
                }
            }
        }
        if (latch.getState() == LeaderLatch.State.CLOSED) {
            log.info("DB Leader选关闭");
        }
        try {
            latch.close();
        } catch (IOException e) {
            log.warn("关闭DB Leader错误", e);
        }
    }

    private void startLeaderJob() throws Exception {
        Stat stat = framework.checkExists().forPath(dbConfig.getZookeeper().getRootPath() + "/db_center");
        if (stat == null) {

        }
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
            log.debug("取消注册服务[{}]", Constants.DB_SERVICE_NAME, e);
        }
        CloseableUtils.closeQuietly(serviceDiscovery);
        if (framework != null) {
            CloseableUtils.closeQuietly(framework);
        }
        log.debug("服务器[{}]取消注册进入服务器", Constants.DB_SERVICE_NAME);
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }
}
