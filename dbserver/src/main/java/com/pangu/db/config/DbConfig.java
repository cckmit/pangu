package com.pangu.db.config;

import lombok.Getter;

@Getter
public class DbConfig {

    private ZookeeperConfig zookeeper;

    private JdbcConfig jdbc;

    private ServerConfig server;

    private int minStartUp;
}
