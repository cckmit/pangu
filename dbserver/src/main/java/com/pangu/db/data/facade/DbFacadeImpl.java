package com.pangu.db.data.facade;

import com.pangu.db.data.service.DbService;
import com.pangu.framework.utils.model.Result;
import com.pangu.model.anno.ServiceDB;
import com.pangu.model.db.EntityRes;

@ServiceDB
public class DbFacadeImpl implements DbFacade {

    private final DbService dbService;

    public DbFacadeImpl(DbService dbService) {
        this.dbService = dbService;
    }

    @Override
    public Result<EntityRes> queryById(String serverId, String table, String idColumnName, Object id) {
        EntityRes res = dbService.queryById(serverId, table, idColumnName, id);
        return Result.SUCCESS(res);
    }
}
