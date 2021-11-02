package com.pangu.db.data.service;

import com.pangu.model.db.EntityRes;

public class DbCache {

    private final String serverId;

    public DbCache(String serverId) {
        this.serverId = serverId;
    }

    public EntityRes queryById(String table, Object id) {
        return null;
    }

    public void shutdown() {

    }
}
