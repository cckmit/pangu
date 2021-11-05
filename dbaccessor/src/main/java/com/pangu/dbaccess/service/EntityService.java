package com.pangu.dbaccess.service;

import com.pangu.core.common.ServerInfo;

import java.io.Serializable;
import java.util.Map;

public class EntityService {

    private final IDbServerAccessor dbServerAccessor;

    public EntityService(IDbServerAccessor dbServerAccessor) {
        this.dbServerAccessor = dbServerAccessor;
    }

    public <PK extends Comparable<PK> & Serializable, T extends IEntity<PK>> T load(String serverId, Class<T> clz, PK pk) {
        Map<String, ServerInfo> dbs = dbServerAccessor.getDbs();
        if (dbs == null || dbs.isEmpty()) {
            throw new IllegalStateException("没有找到任意一个数据库服");
        }

        return null;
    }
}
