package com.pangu.db.data.service;

import com.pangu.model.anno.ServiceDB;
import com.pangu.model.db.EntityRes;
import org.springframework.context.ApplicationContext;

import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ServiceDB
public class DbService {

    private final ApplicationContext context;

    private Set<String> managedServerIds;

    private final ConcurrentHashMap<String, DbCache> caches = new ConcurrentHashMap<>();

    public DbService(ApplicationContext context) {
        this.context = context;
    }

    private DbCache load(String serverId) {
        return caches.computeIfAbsent(serverId, sid -> {
            DbCache cache = new DbCache(sid);
            context.getAutowireCapableBeanFactory().autowireBean(cache);
            cache.init();
            return cache;
        });
    }

    public EntityRes queryById(String serverId, String table, String idColumnName, Object id) {
        if (!managedServerIds.contains(serverId)) {
            return EntityRes.err(serverId + "<server id not be managed");
        }
        DbCache dbCache = load(serverId);

        try {
            return dbCache.queryById(table, idColumnName, id);
        } catch (SQLException thr) {
            return EntityRes.err(thr.getMessage());
        }
    }

    public void updateManagedServerIds(Set<String> managedServerIds) {
        this.managedServerIds = managedServerIds;
    }
}
