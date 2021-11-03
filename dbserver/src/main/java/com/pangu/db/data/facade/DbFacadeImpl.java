package com.pangu.db.data.facade;

import com.pangu.db.data.service.DbService;
import com.pangu.framework.utils.model.Result;
import com.pangu.model.anno.ServiceDB;
import com.pangu.model.db.EntityRes;

import java.util.Map;

@ServiceDB
public class DbFacadeImpl implements DbFacade {

    private final DbService dbService;

    public DbFacadeImpl(DbService dbService) {
        this.dbService = dbService;
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
        dbService.update(serverId, table, idColumnName, id, columns);
        return Result.SUCCESS();
    }
}
