package com.pangu.model.common;

import org.apache.logging.log4j.core.LoggerContext;

public class Log4jCloseThread extends Thread {
    @Override
    public void run() {
        LoggerContext.getContext().close();
    }
}
