package com.pangu.framework.socket.handler.session;

import io.netty.channel.Channel;

/**
 * 鉴权session连接销毁监听
 */
public interface IdentitySessionCloseListener {
    /**
     * 通知
     *
     * @param identity
     */
    void close(Object identity, Channel channel);
}
