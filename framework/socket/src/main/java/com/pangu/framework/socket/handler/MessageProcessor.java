package com.pangu.framework.socket.handler;

import com.pangu.framework.socket.core.Message;

public interface MessageProcessor {

    void process(Session session, Message message);
}
