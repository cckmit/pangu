package com.pangu.db.config;

public class SqlConstant {
    public static final String SELECT_ALL = "SELECT * FROM ";
    public static final String WHERE = " WHERE ";
    public static final String EQUAL = "=?";

    public static String doQuote(String identifier) {
        return "`" + identifier + "`";
    }
}
