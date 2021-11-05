package com.pangu.core.config;

import lombok.Data;

@Data
public class JdbcConfig {

    private String addr;

    private String params;

    private String username;

    private String password;

    private String databasePrefix;

    private String driver = "com.mysql.cj.jdbc.Driver";

    private String urlPrefix = "jdbc:mysql://";
}
