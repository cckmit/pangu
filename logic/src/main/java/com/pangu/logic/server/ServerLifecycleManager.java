package com.pangu.logic.server;

import com.pangu.core.anno.ServiceLogic;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ServiceLogic
@Slf4j
public class ServerLifecycleManager {

    private final List<ServerLifecycle> lifecycles;
    private Set<String> managedServerIds = new HashSet<>();

    public ServerLifecycleManager(List<ServerLifecycle> lifecycles) {
        this.lifecycles = lifecycles;
        this.lifecycles.sort(Comparator.comparingInt(ServerLifecycle::order));
    }

    public void managedServerIdUpdate(Set<String> currentServerIds) {
        if (currentServerIds == null) {
            currentServerIds = new HashSet<>(0);
        }
        this.managedServerIds.removeAll(currentServerIds);
        for (String serverId : this.managedServerIds) {
            for (int index = lifecycles.size() - 1; index >= 0; --index) {
                ServerLifecycle lifecycle = lifecycles.get(index);
                if (!lifecycle.running(serverId)) {
                    continue;
                }
                try {
                    lifecycle.serverEnd(serverId);
                } catch (Throwable thr) {
                    log.error("服务[{}]管理停止异常", serverId, thr);
                }
            }
        }
        this.managedServerIds = currentServerIds;
        for (String serverId : this.managedServerIds) {
            for (ServerLifecycle lifecycle : lifecycles) {
                try {
                    lifecycle.serverStart(serverId);
                } catch (Throwable thr) {
                    log.error("服务[{}]管理启动异常", serverId, thr);
                }
            }
        }
    }
}
