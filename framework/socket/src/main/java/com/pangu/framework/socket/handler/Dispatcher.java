package com.pangu.framework.socket.handler;

import com.pangu.framework.socket.core.Message;
import com.pangu.framework.socket.handler.param.Coder;

import java.util.Map;

public interface Dispatcher {
    void receive(Message message, Session session);

    void register(Object object);

    Coder getDefaultCoder();

    Map<Byte, Coder> getCoders();

    void shutdown();

    void setDefaultCoder(byte json);
}
