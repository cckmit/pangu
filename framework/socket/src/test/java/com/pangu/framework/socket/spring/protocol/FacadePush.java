package com.pangu.framework.socket.spring.protocol;

import com.pangu.framework.socket.anno.InBody;
import com.pangu.framework.socket.anno.PushAllIdentityClient;
import com.pangu.framework.socket.anno.SocketCommand;
import com.pangu.framework.socket.anno.SocketModule;

@SocketModule(1)
public interface FacadePush {

    @SocketCommand(-1)
    @PushAllIdentityClient
    void say(@InBody("hello") String hello);
}
