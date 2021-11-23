package com.pangu.gm;

import com.pangu.core.common.Log4jCloseThread;
import com.pangu.framework.socket.spring.EnableClientFactory;
import com.pangu.framework.utils.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.shell.CommandLine;
import org.springframework.shell.SimpleShellCommandLineOptions;
import org.springframework.shell.core.JLineShellComponent;
import org.springframework.util.StopWatch;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.TimeZone;

@ComponentScan({"com.pangu.gm", "org.springframework.shell.commands", "org.springframework.shell.converters",
        "org.springframework.shell.plugin.support"})
@EnableClientFactory
public class GMApplication {

    public static AnnotationConfigApplicationContext CONTEXT = null;
    private static final StopWatch sw = new StopWatch("GM Shell");

    public static void main(String[] args) {
        System.setProperty("log4j.shutdownHookEnabled", "false");
        Logger log = LoggerFactory.getLogger(GMApplication.class);
        TimeZone timeZone = TimeZone.getDefault();
        log.info("当前系统时区:{},时区名称:{},系统时间:{}", timeZone.getID(), timeZone.getDisplayName(), LocalDateTime.now());
        AnnotationConfigApplicationContext applicationContext = null;
        Log4jCloseThread log4jCloseThread = new Log4jCloseThread();
        Runtime.getRuntime().addShutdownHook(log4jCloseThread);
        try {
            applicationContext = new AnnotationConfigApplicationContext();
            applicationContext.register(GMApplication.class);
            CommandLine commandLine = SimpleShellCommandLineOptions.parseCommandLine(args);
            applicationContext.getBeanFactory().registerSingleton("commandLine", commandLine);
            applicationContext.registerBean("shell", JLineShellComponent.class);
            CONTEXT = applicationContext;
            applicationContext.refresh();
        } catch (Exception e) {
            log.error("初始化服务器应用上下文出错:{}", e.getMessage(), e);
            Runtime.getRuntime().exit(-1);
        }
        Runtime.getRuntime().removeShutdownHook(log4jCloseThread);
        applicationContext.registerShutdownHook();
        applicationContext.start();
        log.info("GM已经启动 - [{}]", DateUtils.date2String(new Date(), DateUtils.PATTERN_DATE_TIME));
    }
}
