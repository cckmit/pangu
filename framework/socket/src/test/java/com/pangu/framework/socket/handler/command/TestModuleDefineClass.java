package com.pangu.framework.socket.handler.command;

import com.pangu.framework.socket.anno.*;
import com.pangu.framework.socket.anno.*;
import com.pangu.framework.utils.model.Result;

import java.util.List;

@SocketModule(1)
public interface TestModuleDefineClass {

    @SocketCommand(7)
    Result<List<String>> search(@Identity Long id, @InBody("name") String name, @InSession("key") String key);
}
