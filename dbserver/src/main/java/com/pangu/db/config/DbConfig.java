package com.pangu.db.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DbConfig {

    private ZookeeperConfig zookeeper;

    private JdbcConfig jdbc;

    private ServerConfig server;
}
