package com.pangu.logic.module.account.model;

import com.pangu.framework.protocol.annotation.Transable;

/**
 * 账号状态
 *
 * @author frank
 */
@Transable
public enum AccountState {

    /**
     * 锁定
     */
    BLOCK(1 << 0),
    /**
     * 游戏管理员
     */
    GM(1 << 1),
    /**
     * 游戏指导员
     */
    GUIDER(1 << 2);

    /**
     * 状态值
     */
    private final int value;

    private AccountState(int value) {
        this.value = value;
    }

    /**
     * 获取该状态对应的状态值
     *
     * @return
     */
    public int getValue() {
        return value;
    }

}
