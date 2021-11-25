package com.pangu.logic.config;

import com.pangu.core.config.ServerConfig;
import com.pangu.core.config.ZookeeperConfig;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogicConfig {

    private ZookeeperConfig zookeeper;

    private ServerConfig socket;

    private SystemConfig system;
}
