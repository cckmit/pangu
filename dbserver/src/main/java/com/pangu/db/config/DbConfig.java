package com.pangu.db.config;

import com.pangu.core.config.JdbcConfig;
import com.pangu.core.config.ServerConfig;
import com.pangu.core.config.ZookeeperConfig;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DbConfig {

    private ZookeeperConfig zookeeper;

    private JdbcConfig jdbc;

    private ServerConfig socket;
}
