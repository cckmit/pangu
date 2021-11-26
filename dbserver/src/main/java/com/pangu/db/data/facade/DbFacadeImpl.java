package com.pangu.db.data.facade;

import com.pangu.core.db.facade.DbFacade;
import com.pangu.db.data.service.DbService;
import com.pangu.db.data.service.OnlineService;
import com.pangu.framework.utils.model.Result;
import com.pangu.core.anno.ServiceDB;
import com.pangu.core.db.model.EntityRes;

import java.util.Map;

@ServiceDB
public class DbFacadeImpl implements DbFacade {

    private final DbService dbService;

    private final OnlineService onlineService;

    public DbFacadeImpl(DbService dbService, OnlineService onlineService) {
        this.dbService = dbService;
        this.onlineService = onlineService;
    }

    @Override
    public Result<EntityRes> load(String serverId, String table, String idColumnName, Object id) {
        EntityRes res = dbService.load(serverId, table, idColumnName, id);
        return Result.SUCCESS(res);
    }

    @Override
    public Result<Integer> insert(String serverId, String table, Object id, Map<String, Object> columns) {
        int insert = dbService.insert(serverId, table, id, columns);
        return Result.ERROR(insert);
    }

    @Override
    public Result<Integer> update(String serverId, String table, String idColumnName, Object id, Map<String, Object> columns) {
        int update = dbService.update(serverId, table, idColumnName, id, columns);
        return Result.ERROR(update);
    }

    @Override
    public Result<Integer> delete(String serverId, String table, String idColumnName, Object id) {
        int delete = dbService.delete(serverId, table, idColumnName, id);
        return Result.ERROR(delete);
    }

    @Override
    public Result<Integer> online(long sessionId, long roleId) {
        onlineService.online(sessionId, roleId);
        return Result.SUCCESS();
    }

    @Override
    public Result<Integer> offline(long sessionId, long roleId) {
        onlineService.offline(sessionId, roleId);
        return Result.SUCCESS();
    }
}
