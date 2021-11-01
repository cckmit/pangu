package com.pangu.framework.socket.spring;

import com.pangu.framework.socket.anno.SocketCommand;
import com.pangu.framework.socket.anno.SocketModule;

@SocketModule(1)
public interface Facade {

    @SocketCommand(2)
    String say(String hello);
}
