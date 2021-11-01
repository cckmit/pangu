package com.pangu.framework.socket.handler.session;

import com.pangu.framework.socket.handler.command.MethodDefine;

import java.util.Collection;

/**
 * 推送接口时，根据业务传进来的Identity，找不到Session
 */
public interface SessionNotFountListener {

    /**
     * 通知
     *
     * @param identity
     * @param methodDefine
     * @param args
     */
    void notify(Collection<Object> identity, MethodDefine methodDefine, Object[] args);
}
