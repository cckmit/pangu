package com.pangu.stress.config;

import com.pangu.core.config.ServerConfig;
import com.pangu.core.config.ZookeeperConfig;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StressConfig {

    private ZookeeperConfig zookeeper;

    private ServerConfig socket;

    private SystemConfig system;
}
