package com.pangu.gm.config;

import org.springframework.shell.plugin.HistoryFileNameProvider;
import org.springframework.stereotype.Component;

@Component
public class CusHistoryProvider implements HistoryFileNameProvider {

    public String getHistoryFileName() {
        return "spring-shell.log";
    }

    public String getProviderName() {
        return "default history provider";
    }
}
