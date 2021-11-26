package com.pangu.gateway.module.service;

import com.pangu.core.anno.ServiceGate;
import com.pangu.framework.socket.handler.Session;
import com.pangu.framework.socket.handler.SessionManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@ServiceGate
public class GateService {

    private final SessionManager sessionManager;

    public GateService(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public Map<Long, Long> online() {
        Collection<Session> allIdentitySession = sessionManager.getAllIdentitySession();
        Map<Long, Long> sessionRole = new HashMap<>();
        for (Session session : allIdentitySession) {
            long sessionId = session.getId();
            Long identity = session.getIdentity();
            if (identity != null) {
                sessionRole.put(sessionId, identity);
            }
        }
        return sessionRole;
    }
}
