package com.pangu.dbaccess.service;

import com.pangu.core.common.ServerInfo;

import java.util.Map;

public interface IDbServerAccessor {

    /**
     * 返回DB服列表
     *
     * @return 服列表 <dbServerId, 服信息>
     */
    Map<String, ServerInfo> getDbs();

    /**
     * 返回每个数据服管理的数据库id
     *
     * @return <database_id, dbServerId>
     */
    Map<String, String> getDbManagedServer();
}
