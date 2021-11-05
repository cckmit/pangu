package com.pangu.core.db.facade;

import com.pangu.framework.socket.anno.SocketDefine;

@SocketDefine
public interface DbModule {
    // 数据库操作
    int MODULE = 1;

    // 根据玩家所在实体ID查询
    int QUERY_BY_ID = 1;

    // 插入数据
    int INSERT = 2;

    // 通过ID保存
    int UPDATE = 3;

    // 删除
    int DELETE = 4;
}
