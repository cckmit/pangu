package com.pangu.db.data.facade;

import com.pangu.framework.socket.anno.InBody;
import com.pangu.framework.socket.anno.SocketCommand;
import com.pangu.framework.socket.anno.SocketModule;
import com.pangu.framework.utils.model.Result;
import com.pangu.model.db.EntityRes;

/**
 * 数据服接口
 */
@SocketModule(DbModule.MODULE)
public interface DbFacade {

    /**
     * 根据服id，以及表ID查询实体数据
     *
     * @param serverId 区服ID
     * @param id       实体ID
     * @return 查询内容
     */
    @SocketCommand(DbModule.QUERY_BY_ID)
    Result<EntityRes> queryById(@InBody String serverId, @InBody String table, @InBody Object id);
}
