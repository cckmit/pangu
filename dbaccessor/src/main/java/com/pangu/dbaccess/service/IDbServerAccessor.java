package com.pangu.dbaccess.service;

import com.pangu.core.common.ServerInfo;

import java.util.Map;

public interface IDbServerAccessor {

    /**
     * 返回DB服列表
     *
     * @return
     */
    Map<String, ServerInfo> getDbs();
}
