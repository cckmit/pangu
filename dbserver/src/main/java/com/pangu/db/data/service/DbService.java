package com.pangu.db.data.service;

import com.pangu.core.db.facade.DbResult;
import com.pangu.core.anno.ServiceDB;
import com.pangu.core.db.model.EntityRes;
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

    private final ConcurrentHashMap<String, DbAccessor> caches = new ConcurrentHashMap<>();

    public DbService(ApplicationContext context) {
        this.context = context;
    }

    private DbAccessor load(String serverId) {
        return caches.computeIfAbsent(serverId, sid -> {
            DbAccessor cache = new DbAccessor(sid);
            context.getAutowireCapableBeanFactory().autowireBean(cache);
            cache.init();
            return cache;
        });
    }

    public EntityRes load(String serverId, String table, String idColumnName, Object id) {
        if (!managedServerIds.contains(serverId)) {
            return EntityRes.err(serverId + "<server id not be managed");
        }
        DbAccessor dbAccessor = load(serverId);
        try {
            return dbAccessor.queryById(table, idColumnName, id);
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
        DbAccessor dbAccessor = load(serverId);
        try {
            int insert = dbAccessor.insert(table, columns);
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
        DbAccessor dbAccessor = load(serverId);
        try {
            int insert = dbAccessor.update(table, idColumnName, id, columns);
            if (insert >= 1) {
                return 0;
            }
            return DbResult.UPDATE_FAIL;
        } catch (SQLException throwables) {
            log.warn("更新数据异常[{}][{}][{}][{}]", serverId, table, id, columns);
            return DbResult.SQL_EXCEPTION;
        }
    }

    public int delete(String serverId, String table, String idColumnName, Object id) {
        if (!managedServerIds.contains(serverId)) {
            return DbResult.NOT_MANAGED_SERVER_ID;
        }
        DbAccessor dbAccessor = load(serverId);
        try {
            int insert = dbAccessor.delete(table, idColumnName, id);
            if (insert >= 1) {
                return 0;
            }
            return DbResult.UPDATE_FAIL;
        } catch (SQLException throwables) {
            log.warn("删除数据异常[{}][{}][{}]", serverId, table, id);
            return DbResult.SQL_EXCEPTION;
        }
    }
}
