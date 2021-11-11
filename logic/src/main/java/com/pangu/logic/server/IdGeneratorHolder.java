package com.pangu.logic.server;

import com.pangu.core.anno.ComponentLogic;
import com.pangu.framework.utils.id.IdGenerator;

import java.util.concurrent.ConcurrentHashMap;

@ComponentLogic
public class IdGeneratorHolder {

    private volatile boolean init;

    private final ConcurrentHashMap<String, IdGenerator> idGenerators = new ConcurrentHashMap<>();

    private final LogicServerManager logicServerManager;

    public IdGeneratorHolder(LogicServerManager logicServerManager) {
        this.logicServerManager = logicServerManager;
    }

    public long getNext(String userServerId) {
        IdGenerator idGenerator = idGenerators.get(userServerId);
        if (idGenerator == null || idGenerator.getCurrent() == 0) {
            idGenerator = idGenerators.computeIfAbsent(userServerId, k -> {
                int[] oidSid = IdGenerator.toOperatorServer(userServerId);
                int logicServerId = logicServerManager.getServerId();
                return new IdGenerator(oidSid[0], oidSid[1], logicServerId, 0L);
            });
            if (idGenerator.getCurrent() == 0) {
                //noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (idGenerator) {
                    if (idGenerator.getCurrent() == 0) {
                        long value = logicServerManager.getManagedServerIndex(userServerId);
                        idGenerator.update(value);
                    }
                }
            }
        }
        long next = idGenerator.getNext();
        logicServerManager.saveManagedServerIndex(userServerId, idGenerator.getCurrent());
        return next;
    }
}
