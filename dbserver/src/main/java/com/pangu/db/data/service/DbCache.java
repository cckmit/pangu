package com.pangu.db.data.service;

import com.pangu.db.config.DbConfig;
import com.pangu.db.config.JdbcConfig;
import com.pangu.db.config.SqlConstant;
import com.pangu.model.db.EntityRes;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import static com.pangu.db.config.SqlConstant.doQuote;

@Slf4j
public class DbCache {

    @Autowired
    @Setter(AccessLevel.PACKAGE)
    private DbConfig dbConfig;

    private HikariDataSource dataSource;

    private final String serverId;

    private volatile boolean init = false;
    private String dbName;

    public DbCache(String serverId) {
        this.serverId = serverId;
    }

    public void init() {
        if (init) {
            return;
        }
        synchronized (this) {
            if (!init) {
                HikariDataSource dataSource = new HikariDataSource();
                JdbcConfig jdbc = dbConfig.getJdbc();
                dataSource.setDriverClassName(jdbc.getDriver());
                dataSource.setJdbcUrl(jdbc.getUrlPrefix() + jdbc.getAddr() + "/" + dbName + jdbc.getParams());
                dataSource.setUsername(jdbc.getUsername());
                dataSource.setPassword(jdbc.getPassword());
                dataSource.setConnectionTestQuery("SELECT 1");
                dataSource.setValidationTimeout(3_000);
                dataSource.setReadOnly(false);
                dataSource.setConnectionTimeout(30_000);
                dataSource.setIdleTimeout(60_000);
                dataSource.setMaxLifetime(180_000);
                dataSource.setMaximumPoolSize(20);
                dataSource.setMinimumIdle(5);
                dataSource.setRegisterMbeans(true);

                this.dataSource = dataSource;
                this.dbName = dbConfig.getJdbc().getDatabasePrefix() + serverId;
                init = true;
            }
        }
    }

    public EntityRes queryById(String table, String idColumnName, Object id) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    SqlConstant.SELECT_ALL + doQuote(table) + SqlConstant.WHERE + doQuote(idColumnName) + SqlConstant.EQUAL
            )) {
                if (id instanceof Number) {
                    statement.setLong(1, ((Number) id).longValue());
                } else if (id instanceof String) {
                    statement.setString(1, (String) id);
                } else {
                    String msg = String.format("数据库查询字段必须为String或者Number,参数[%s][%s][%s][%s]", dbName, table, idColumnName, id);
                    log.warn(msg);
                    return EntityRes.err(msg);
                }
                try (ResultSet resultSet = statement.executeQuery()) {
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    Map<String, Object> row = null;
                    while (resultSet.next()) {
                        if (row != null) {
                            log.warn("DB Server通过ID查询返回多个结果[{}][{}][{}][{}]", dbName, table, idColumnName, id);
                            break;
                        }
                        row = new HashMap<>(columnCount);
                        for (int i = 1; i <= columnCount; ++i) {
                            Object colValue = resultSet.getObject(i);
                            String columnName = metaData.getColumnName(i);
                            row.put(columnName, colValue);
                        }
                    }
                    return EntityRes.of(row);
                }
            }
        }
    }

    public void shutdown() {
        this.dataSource.close();
    }

    public int insert(String table, Map<String, Object> columns) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            StringBuilder columnInsert = new StringBuilder(64).append("INSERT INTO ").append(doQuote(table)).append("(");
            StringBuilder valuesInsert = new StringBuilder(32).append(" VALUES (");
            for (Map.Entry<String, Object> entry : columns.entrySet()) {
                String k = entry.getKey();
                columnInsert.append(doQuote(k)).append(",");
                valuesInsert.append("?,");
            }
            columnInsert.deleteCharAt(columnInsert.length() - 1);
            valuesInsert.deleteCharAt(valuesInsert.length() - 1);
            columnInsert.append(")");
            valuesInsert.append(")");

            columnInsert.append(valuesInsert);

            try (PreparedStatement statement = connection.prepareStatement(columnInsert.toString())) {
                int index = 0;
                for (Map.Entry<String, Object> entry : columns.entrySet()) {
                    Object value = entry.getValue();
                    statement.setObject(index, value);
                    ++index;
                }
                return statement.executeUpdate();
            }
        }
    }

    public int update(String table, String idColumnName, Object id, Map<String, Object> columns) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            StringBuilder columnInsert = new StringBuilder(64).append("UPDATE ").append(doQuote(table)).append(" SET ");
            for (Map.Entry<String, Object> entry : columns.entrySet()) {
                String k = entry.getKey();
                columnInsert.append(doQuote(k)).append("=? ");
            }
            columnInsert.append("WHERE ").append(doQuote(idColumnName)).append("=?");

            try (PreparedStatement statement = connection.prepareStatement(columnInsert.toString())) {
                int index = 0;
                for (Map.Entry<String, Object> entry : columns.entrySet()) {
                    Object value = entry.getValue();
                    statement.setObject(index, value);
                    ++index;
                }
                statement.setObject(index, id);
                return statement.executeUpdate();
            }
        }
    }
}
