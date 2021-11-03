package com.pangu.db.data.service;

import com.pangu.db.data.facade.DbResult;
import com.pangu.model.anno.ServiceDB;
import com.pangu.model.db.EntityRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ServiceDB
@Slf4j
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

    public EntityRes load(String serverId, String table, String idColumnName, Object id) {
        if (!managedServerIds.contains(serverId)) {
            return EntityRes.err(serverId + "<server id not be managed");
        }
        DbCache dbCache = load(serverId);
        try {
            return dbCache.queryById(table, idColumnName, id);
        } catch (SQLException thr) {
            log.warn("查询数据异常[{}][{}][{}][{}]", serverId, table, idColumnName, id, thr);
            return EntityRes.err(thr.getMessage());
        }
    }

    public void updateManagedServerIds(Set<String> managedServerIds) {
        this.managedServerIds = managedServerIds;
    }

    public int insert(String serverId, String table, Object id, Map<String, Object> columns) {
        if (!managedServerIds.contains(serverId)) {
            return DbResult.NOT_MANAGED_SERVER_ID;
        }
        DbCache dbCache = load(serverId);
        try {
            int insert = dbCache.insert(table, columns);
            if (insert >= 1) {
                return 0;
            }
            return DbResult.UPDATE_FAIL;
        } catch (SQLException throwables) {
            log.warn("插入数据异常[{}][{}][{}][{}]", serverId, table, id, columns);
            return DbResult.SQL_EXCEPTION;
        }
    }

    public int update(String serverId, String table, String idColumnName, Object id, Map<String, Object> columns) {
        if (!managedServerIds.contains(serverId)) {
            return DbResult.NOT_MANAGED_SERVER_ID;
        }
        DbCache dbCache = load(serverId);
        try {
            int insert = dbCache.update(table, idColumnName, id, columns);
            if (insert >= 1) {
                return 0;
            }
            return DbResult.UPDATE_FAIL;
        } catch (SQLException throwables) {
            log.warn("插入数据异常[{}][{}][{}][{}]", serverId, table, id, columns);
            return DbResult.SQL_EXCEPTION;
        }
    }
}
