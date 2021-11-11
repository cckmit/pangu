package com.pangu.core.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerConfig {
    private String address;
    private int coder;
    private String bootstrap;
    private boolean manageThread;
    private int serverRead;
    private int serverWrite;
    private int backlog;
    private String bossThreadName;
    private String workThreadName;
    private boolean sslServerEnable;
    private String sslPassword;
    private String sslStoreType;
    private String sslStorePath;

    private int clientRead;
    private int clientWrite;
    private int clientTimeoutConnect;
    private int clientTimeoutRead;
    private String clientWorkThreadName;
    private String clientWorkThreadAmount;
    private boolean heartBeat;
    private int heartBeatIntervalMs;
    private int keepAliveMs;
    private boolean sslClientEnable;
}
