package com.pangu.core.gate.facade;

import com.pangu.framework.socket.anno.SocketCommand;
import com.pangu.framework.socket.anno.SocketModule;
import com.pangu.framework.utils.model.Result;

import java.util.Map;

@SocketModule(GateModule.MODULE)
public interface GateFacade {

    /**
     * 查询在线玩家
     *
     * @return
     */
    @SocketCommand(GateModule.ONLINE)
    Result<Map<Long, Long>> online();
}
