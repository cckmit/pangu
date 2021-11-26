package com.pangu.core.gate.facade;

import com.pangu.framework.socket.anno.SocketDefine;

@SocketDefine
public interface GateModule {
    int MODULE = 2;

    // 查询在线玩家
    int ONLINE = 1;
}
