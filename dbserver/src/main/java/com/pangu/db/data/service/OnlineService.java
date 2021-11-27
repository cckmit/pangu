package com.pangu.db.data.service;

import com.pangu.core.anno.ComponentDb;
import com.pangu.core.common.ServerInfo;
import com.pangu.core.gate.facade.GateFacade;
import com.pangu.framework.socket.client.Client;
import com.pangu.framework.socket.client.ClientFactory;
import com.pangu.framework.socket.utils.ServerIdGenerator;
import com.pangu.framework.utils.collection.ConcurrentHashSet;
import com.pangu.framework.utils.id.IdGenerator;
import com.pangu.framework.utils.model.Result;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ComponentDb
@Getter
@Slf4j
public class OnlineService {

    private final ClientFactory clientFactory;

    private final ConcurrentHashMap<Long, Long> roleSession = new ConcurrentHashMap<>(3000);
    private final ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, ConcurrentHashSet<Long>>> oidSidRole = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Long, Long> sessionRole = new ConcurrentHashMap<>(3000);
    private final ConcurrentHashMap<Integer, ConcurrentHashSet<Long>> gateRoleIds = new ConcurrentHashMap<>();

    public OnlineService(ClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    public void online(long sessionId, long roleId) {
        Long preSessionId = roleSession.put(roleId, sessionId);
        IdGenerator.IdInfo idInfo = new IdGenerator.IdInfo(roleId);
        int operator = idInfo.getOperator();
        ConcurrentHashMap<Integer, ConcurrentHashSet<Long>> sidRole = oidSidRole.computeIfAbsent(operator, k -> new ConcurrentHashMap<>());
        ConcurrentHashSet<Long> roles = sidRole.computeIfAbsent((int) idInfo.getServer(), k -> new ConcurrentHashSet<>());
        roles.add(roleId);

        if (preSessionId != null && preSessionId != sessionId) {
            sessionRole.remove(preSessionId, roleId);
            int severId = ServerIdGenerator.toServerId(preSessionId);
            ConcurrentHashSet<Long> cRole = gateRoleIds.computeIfAbsent(severId, k -> new ConcurrentHashSet<>(1000));
            cRole.remove(roleId);
        }
        sessionRole.put(sessionId, roleId);
        int serverId = ServerIdGenerator.toServerId(sessionId);
        ConcurrentHashSet<Long> roleIds = gateRoleIds.computeIfAbsent(serverId, k -> new ConcurrentHashSet<>());
        roleIds.add(roleId);
    }

    public void offline(long sessionId, long roleId) {
        boolean remove = roleSession.remove(roleId, sessionId);
        if (remove) {
            IdGenerator.IdInfo idInfo = new IdGenerator.IdInfo(roleId);
            int operator = idInfo.getOperator();
            ConcurrentHashMap<Integer, ConcurrentHashSet<Long>> sidRole = oidSidRole.computeIfAbsent(operator, k -> new ConcurrentHashMap<>());
            ConcurrentHashSet<Long> roles = sidRole.computeIfAbsent((int) idInfo.getServer(), k -> new ConcurrentHashSet<>());
            roles.remove(roleId);
        }
        boolean suc = sessionRole.remove(sessionId, roleId);
        if (suc) {
            int serverId = ServerIdGenerator.toServerId(sessionId);
            ConcurrentHashSet<Long> roleIds = gateRoleIds.computeIfAbsent(serverId, k -> new ConcurrentHashSet<>());
            roleIds.remove(roleId);
        }
    }

    public void gateUpdate(Set<ServerInfo> addGate, Set<ServerInfo> removedGate) {
        for (ServerInfo serverInfo : removedGate) {
            String id = serverInfo.getId();
            int serverId = Integer.parseInt(id);
            gateOffLine(serverId);
        }
        for (ServerInfo serverInfo : addGate) {
            try {
                gateOnline(serverInfo);

            } catch (Throwable throwable) {
                log.warn("查询网关服[{}][{}]在线玩家失败，请检查", serverInfo.getId(), serverInfo.getAddress(), throwable);
            }
        }
    }

    public void gateOffLine(int gateId) {
        ConcurrentHashSet<Long> roleIds = gateRoleIds.remove(gateId);
        if (roleIds == null) {
            return;
        }
        for (Long roleId : roleIds) {
            Long sessionId = roleSession.remove(roleId);
            IdGenerator.IdInfo idInfo = new IdGenerator.IdInfo(roleId);
            int operator = idInfo.getOperator();
            ConcurrentHashMap<Integer, ConcurrentHashSet<Long>> sidRole = oidSidRole.computeIfAbsent(operator, k -> new ConcurrentHashMap<>());
            ConcurrentHashSet<Long> roles = sidRole.computeIfAbsent((int) idInfo.getServer(), k -> new ConcurrentHashSet<>());
            roles.remove(roleId);

            if (sessionId != null) {
                sessionRole.remove(sessionId, roleId);
            }
        }
    }

    private void gateOnline(ServerInfo serverInfo) {
        Client client = clientFactory.getClient(serverInfo.getAddress());
        Result<Map<Long, Long>> onlineRes = client.getProxy(GateFacade.class).online();
        if (onlineRes == null) {
            log.warn("查询网关服[{}][{}]在线玩家失败，请检查", serverInfo.getId(), serverInfo.getAddress());
            return;
        }
        if (onlineRes.getCode() < 0) {
            log.info("查询网关服[{}][{}]在线玩家ManagedException[{}]", serverInfo.getId(), serverInfo.getAddress(), onlineRes.getCode());
            return;
        }
        Map<Long, Long> content = onlineRes.getContent();
        log.debug("刷新网关[{}][{}]在线人数[{}]", serverInfo.getId(), serverInfo.getAddress(), content == null ? 0 : content.size());
        if (content == null) {
            return;
        }
        for (Map.Entry<Long, Long> entry : content.entrySet()) {
            Long sessionId = entry.getKey();
            Long roleId = entry.getValue();
            online(sessionId, roleId);
        }
    }
}
