package com.pangu.gateway.rout;

import com.pangu.core.config.ServerConfig;
import com.pangu.framework.socket.handler.SslConfig;
import com.pangu.framework.socket.server.SocketServer;
import com.pangu.framework.socket.server.SocketServerBuilder;

public class GateServer {

    private final ServerConfig serverConfig;
    private SocketServer socketServer;

    public GateServer(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public void start() {
        SocketServerBuilder socketServerBuilder = new SocketServerBuilder();
        socketServerBuilder.address(serverConfig.getAddress())
                .readBuffSize(serverConfig.getServerRead())
                .writeBuffSize(serverConfig.getServerWrite());
        if (serverConfig.isSslServerEnable()) {
            socketServerBuilder.sslConfig(SslConfig.server(true,
                    serverConfig.getSslPassword(),
                    serverConfig.getSslStoreType(),
                    serverConfig.getSslStorePath())
            );
        }
        socketServer = socketServerBuilder.build();
        socketServer.start();
    }

    public void stop() {
        if (socketServer != null) {
            socketServer.stop();
        }
    }
}
