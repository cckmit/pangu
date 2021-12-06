package com.pangu.logic.server;

public interface ServerLifecycle {
    default int order() {
        return 0;
    }

    void serverStart(String serverId);

    void serverEnd(String serverId);

    boolean running(String serverId);
}
