package com.pangu.logic.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.pangu.core.anno.ServiceLogic;
import com.pangu.core.common.Constants;
import com.pangu.core.common.InstanceDetails;
import com.pangu.core.common.ServerInfo;
import com.pangu.core.config.ZookeeperConfig;
import com.pangu.dbaccess.service.IDbServerAccessor;
import com.pangu.framework.utils.json.JsonUtils;
import com.pangu.framework.utils.lang.ByteUtils;
import com.pangu.framework.utils.os.NetUtils;
import com.pangu.logic.config.LogicConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.RetryForever;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.*;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.details.ServiceCacheListener;
import org.apache.zookeeper.KeeperException;
import org.springframework.context.Lifecycle;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@ServiceLogic
@Slf4j
public class LogicServerManager implements Lifecycle, IDbServerAccessor {

    private final LogicConfig logicConfig;

    private final AtomicBoolean running = new AtomicBoolean();
    private CuratorFramework framework;
    private ServiceInstance<InstanceDetails> serviceInstance;
    private ServiceDiscovery<InstanceDetails> serviceDiscovery;
    private ServiceCache<InstanceDetails> serverCache;
    private Map<String, ServerInfo> dbServers = new HashMap<>(0);
    private Map<String, String> gameServerToDb = new HashMap<>(0);

    private final int serverId;

    private volatile long preSavedIdx;

    public LogicServerManager(LogicConfig logicConfig) {
        this.logicConfig = logicConfig;
        this.serverId = logicConfig.getZookeeper().getServerId();
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
        int id = logicConfig.getZookeeper().getServerId();

        ServiceInstanceBuilder<InstanceDetails> builder = ServiceInstance.<InstanceDetails>builder()
                .id(String.valueOf(id))
                .name(Constants.LOGIC_SERVICE_NAME)
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
                .name(Constants.DB_SERVICE_NAME)
                .build();
        serverCache.start();

        initDBServerService(serverCache);

        log.debug("首次刷新数据服务器列表[{}]", dbServers);
        serverCache.addListener(new ServiceCacheListener() {
            @Override
            public void cacheChanged() {
                initDBServerService(serverCache);
            }

            @Override
            public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
                log.debug("Zookeeper状态改变[{}]", connectionState);
            }
        });
    }

    private void initDBServerService(ServiceCache<InstanceDetails> cache) {
        List<ServiceInstance<InstanceDetails>> instances = cache.getInstances();
        Map<String, ServerInfo> servers = new HashMap<>();
        Map<String, String> serverIdToDB = new HashMap<>();
        for (ServiceInstance<InstanceDetails> instance : instances) {
            InstanceDetails payload = instance.getPayload();
            String sid = instance.getId();
            ServerInfo serverInfo = new ServerInfo(sid, instance.getAddress(), instance.getPort(), payload.getAddressForClient(), payload);
            servers.put(sid, serverInfo);
            String description = payload.getDescription();
            if (StringUtils.isBlank(description)) {
                log.debug("DB服未发送管理节点ID，忽视[{}]", serverInfo.getId());
                continue;
            }
            Set<String> managedServerIds = JsonUtils.string2GenericObject(description, new TypeReference<Set<String>>() {
            });

            if (managedServerIds != null) {
                for (String gameServerId : managedServerIds) {
                    String pre = serverIdToDB.put(gameServerId, sid);
                    if (pre != null) {
                        log.error("相同的serverId[{}]绑定，当前值[{}],存在值[{}]", gameServerId, sid, pre);
                    }
                }
            }
        }
        dbServers = servers;
        gameServerToDb = serverIdToDB;
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

    @Override
    public Map<String, ServerInfo> getDbs() {
        return dbServers;
    }

    @Override
    public Map<String, String> getDbManagedServer() {
        return gameServerToDb;
    }

    public int getServerId() {
        return serverId;
    }

    public long getManagedServerIndex(String userServerId) {
        ZookeeperConfig zookeeper = logicConfig.getZookeeper();
        String path = zookeeper.getRootPath()
                + Constants.IDX_MANAGE
                + zookeeper.getServerId()
                + "/" + userServerId;
        try {
            byte[] bytes = framework.getData().forPath(path);
            return ByteUtils.longFromByte(bytes);
        } catch (Throwable exp) {
            if (exp instanceof KeeperException) {
                try {
                    framework.create().creatingParentsIfNeeded().forPath(path, ByteUtils.longToByte(1));
                } catch (Exception e) {
                    log.info("创建错误", e);
                }
            } else {
                log.info("查询IDx错误[{}]", userServerId);
                throw new RuntimeException(exp);
            }
        }
        preSavedIdx = 1;
        return 1;
    }

    public void saveManagedServerIndex(String userServerId, long index) {
        if (index <= preSavedIdx) {
            return;
        }
        ZookeeperConfig zookeeper = logicConfig.getZookeeper();
        String path = zookeeper.getRootPath()
                + Constants.IDX_MANAGE
                + zookeeper.getServerId()
                + "/" + userServerId;
        preSavedIdx = index + 1000;
        try {
            framework.setData().inBackground().withUnhandledErrorListener((message, e) -> {
                log.info("设置保存Idx异常[{}]", message, e);
            }).forPath(path, ByteUtils.longToByte(preSavedIdx));
        } catch (Exception e) {
            log.info("保存IDx错误[{}]", userServerId, e);
        }
    }
}
