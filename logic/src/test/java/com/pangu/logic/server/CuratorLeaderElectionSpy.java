package com.pangu.logic.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.pangu.core.common.Constants;
import com.pangu.core.config.ZookeeperConfig;
import com.pangu.framework.utils.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.RetryForever;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class CuratorLeaderElectionSpy {
    public static void main(String[] args) {
        int count = 5;
        List<LeaderJob> leaderJobs = new ArrayList<>(count);
        for (int i = 1; i <= count; ++i) {
            LeaderJob leaderJob = new LeaderJob(i);
            leaderJobs.add(leaderJob);
            Thread t = new Thread(leaderJob);
            t.setDaemon(true);
            t.start();
        }
        try {
            Thread.sleep(10_0000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static class LeaderJob implements Runnable {

        private LeaderSelector leaderSelector;
        private CuratorFramework framework;
        private final String rootPath = "/shennu-spy";
        private int id;

        public LeaderJob(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            framework = CuratorFrameworkFactory.builder()
                    .connectString("localhost:2181")
                    .sessionTimeoutMs(20_000)
                    .connectionTimeoutMs(10_000)
                    .retryPolicy(new RetryForever(3000))
                    .build();
            framework.start();
            log.info("[{}]开始Leader争夺", id);
            leaderSelector = new LeaderSelector(framework, rootPath + Constants.LOGIC_LEADER_PATH, new LeaderSelectorListener() {
                @Override
                public void takeLeadership(CuratorFramework client) {
                    log.info("[{}]开始执行Leader职责", id);
                    String manageServerParentPath = rootPath + Constants.DB_MANAGE_LIST;
                    try {
                        Stat stat = framework.checkExists().forPath(manageServerParentPath);
                        if (stat == null) {
                            framework.create().forPath(manageServerParentPath);
                        }

                        CuratorCache cache = CuratorCache.builder(framework, manageServerParentPath).build();
                        cache.listenable().addListener(new CuratorCacheListener() {
                            @Override
                            public void event(Type type, ChildData oldData, ChildData data) {
                                refreshNodes(type, cache);
                            }
                        });
                        cache.start();
                        int i = 1;
                        while (leaderSelector.hasLeadership() && framework.getZookeeperClient().isConnected()) {
                            try {
                                //noinspection BusyWait
                                Thread.sleep(1000);
                                ++i;
                                if (i >= 10) {
                                    break;
                                }
                            } catch (InterruptedException inter) {
                                break;
                            }
                        }
                        cache.close();
                    } catch (Exception e) {
                        log.error("DB Server执行主服逻辑异常", e);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ignore) {

                        }
                    }
                    log.info("[{}]结束LeaderJob", id);
                }

                private void refreshNodes(CuratorCacheListener.Type type, CuratorCache cache) {
                    Set<ChildData> collect = cache.stream().collect(Collectors.toSet());
                    Set<String> serverIds = new HashSet<>();
                    String manageServerParentPath = rootPath + Constants.DB_MANAGE_LIST;
                    for (ChildData data : collect) {
                        if (data.getPath().equals(manageServerParentPath)) {
                            continue;
                        }
                        byte[] bytes = data.getData();
                        if (bytes == null || bytes.length == 0) {
                            return;
                        }

                        Set<String> managedServerIds = JsonUtils.bytes2GenericObject(bytes, new TypeReference<Set<String>>() {
                        });
                        serverIds.addAll(managedServerIds);
                    }
                    log.info("[{}]最新服务器列表，更新[{}],列表[{}]", id, type, serverIds);
                }

                @Override
                public void stateChanged(CuratorFramework client, ConnectionState newState) {
                }
            });
            try {
                leaderSelector.start();
                leaderSelector.autoRequeue();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void close() {
            if (leaderSelector != null) {
                leaderSelector.close();
            }
            if (framework != null) {
                framework.close();
            }
        }
    }
}
