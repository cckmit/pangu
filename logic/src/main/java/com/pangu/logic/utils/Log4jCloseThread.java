package  com.pangu.logic.utils;

import org.apache.logging.log4j.core.LoggerContext;

public class Log4jCloseThread extends Thread {
    @Override
    public void run() {
        LoggerContext.getContext().close();
    }
}
