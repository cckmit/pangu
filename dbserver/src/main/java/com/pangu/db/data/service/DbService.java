package com.pangu.db.data.service;

import com.pangu.model.anno.ServiceDB;
import com.pangu.model.db.EntityRes;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.ConcurrentHashMap;

@ServiceDB
public class DbService {

    private final ApplicationContext context;

    private ConcurrentHashMap<String, DbCache> caches = new ConcurrentHashMap<>();

    public DbService(ApplicationContext context) {
        this.context = context;
    }

    public EntityRes queryById(String serverId, String table, Object id) {
        DbCache dbCache = caches.computeIfAbsent(serverId, sid -> {
            DbCache cache = new DbCache(serverId);
            context.getAutowireCapableBeanFactory().autowireBean(cache);
            return cache;
        });
        DbCache pre = caches.putIfAbsent(serverId, dbCache);
        if (pre != null) {
            dbCache.shutdown();
            dbCache = pre;
        }
        return dbCache.queryById(table, id);
    }
}
