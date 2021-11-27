package com.pangu.framework.socket.handler.session;

import com.pangu.framework.socket.handler.Session;

/**
 * 鉴权session连接监听
 */
public interface IdentitySessionListener {
    /**
     * 通知
     *
     * @param identity
     * @param session
     */
    void identity(long identity, Session session);
}
