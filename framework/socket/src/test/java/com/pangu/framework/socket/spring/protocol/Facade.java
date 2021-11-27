package com.pangu.framework.socket.spring.protocol;

import com.pangu.framework.socket.anno.InBody;
import com.pangu.framework.socket.anno.SocketCommand;
import com.pangu.framework.socket.anno.SocketModule;
import com.pangu.framework.socket.handler.Session;

@SocketModule(1)
public interface Facade {

    @SocketCommand(2)
    PlayerVo say(Session session, @InBody("hello") String hello);
}
