package com.pangu.dbaccess.service;

public interface IEntity<PK> {
    /**
     * 获取实体标识
     *
     * @return
     */
    PK getId();
}
