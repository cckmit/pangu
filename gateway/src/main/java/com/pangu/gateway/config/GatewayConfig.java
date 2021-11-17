package com.pangu.gateway.config;

import com.pangu.core.config.ServerConfig;
import com.pangu.core.config.ZookeeperConfig;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GatewayConfig {

    private ZookeeperConfig zookeeper;

    private ServerConfig socket;

    private RoutConfig rout;
}
