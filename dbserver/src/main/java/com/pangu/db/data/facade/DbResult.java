package com.pangu.db.data.facade;

import com.pangu.framework.protocol.annotation.Constant;

@Constant
public interface DbResult {
    // 服ID不被此数据服管理
    int NOT_MANAGED_SERVER_ID = -1001;

    // sql执行异常
    int SQL_EXCEPTION = -1002;

    // 插入失败
    int UPDATE_FAIL = -1003;
}
