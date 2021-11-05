package com.pangu.db.data.facade;

import com.pangu.framework.socket.anno.InBody;
import com.pangu.framework.socket.anno.SocketCommand;
import com.pangu.framework.socket.anno.SocketModule;
import com.pangu.framework.utils.model.Result;
import com.pangu.core.db.EntityRes;

import java.util.Map;

/**
 * 数据服接口
 */
@SocketModule(DbModule.MODULE)
public interface DbFacade {

    /**
     * 根据服id，以及表ID查询实体数据
     *
     * @param serverId     区服ID
     * @param table        表名
     * @param idColumnName ID字段名
     * @param id           实体ID
     * @return 查询内容
     */
    @SocketCommand(DbModule.QUERY_BY_ID)
    Result<EntityRes> load(@InBody String serverId, @InBody String table, @InBody String idColumnName, @InBody Object id);

    /**
     * 插入数据
     *
     * @param serverId 区服ID
     * @param table    表名
     * @param id       实体ID
     * @param columns  每列数据
     * @return 是否保存成功
     */
    @SocketCommand(DbModule.INSERT)
    Result<Integer> insert(@InBody String serverId, @InBody String table, @InBody Object id, @InBody Map<String, Object> columns);

    /**
     * 通过ID保存
     *
     * @param serverId     区服ID
     * @param table        表名
     * @param idColumnName ID字段名
     * @param id           实体ID
     * @param columns      每列数据
     * @return 是否保存成功
     */
    @SocketCommand(DbModule.UPDATE)
    Result<Integer> update(@InBody String serverId, @InBody String table, @InBody String idColumnName, @InBody Object id, @InBody Map<String, Object> columns);

    /**
     * 通过ID删除
     *
     * @param serverId     区服ID
     * @param table        表名
     * @param idColumnName ID字段名
     * @param id           实体ID
     * @return 是否删除成功
     */
    @SocketCommand(DbModule.DELETE)
    Result<Integer> delete(@InBody String serverId, @InBody String table, @InBody String idColumnName, @InBody Object id);
}
