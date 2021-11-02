package com.pangu.db.config;

import lombok.Data;

@Data
public class JdbcConfig {

    private String addr;

    private String params;

    private String username;

    private String password;
}
