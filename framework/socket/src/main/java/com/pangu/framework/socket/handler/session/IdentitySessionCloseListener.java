package com.pangu.framework.socket.handler.session;

import com.pangu.framework.socket.handler.Session;
import io.netty.channel.Channel;

/**
 * 鉴权session连接销毁监听
 */
public interface IdentitySessionCloseListener {
    /**
     * 通知
     *
     * @param identity
     * @param session
     * @param channel
     */
    void close(long identity, Session session, Channel channel);
}
