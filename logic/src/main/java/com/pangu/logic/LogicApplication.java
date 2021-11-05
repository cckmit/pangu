package com.pangu.logic;

import com.pangu.framework.utils.time.DateUtils;
import com.pangu.core.common.Log4jCloseThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@ComponentScan("com.pangu.logic")
public class LogicApplication {

    public static ApplicationContext CONTEXT;

    private static final ReentrantLock LOCK = new ReentrantLock();
    private static final Condition stop = LOCK.newCondition();

    public static void main(String[] args) {
        System.setProperty("log4j.shutdownHookEnabled", "false");
        System.setProperty("spring.profiles.active", "logic");
        Logger log = LoggerFactory.getLogger(LogicApplication.class);
        TimeZone timeZone = TimeZone.getDefault();
        log.info("当前系统时区:{},时区名称:{},系统时间:{}", timeZone.getID(), timeZone.getDisplayName(), LocalDateTime.now());
        AnnotationConfigApplicationContext applicationContext = null;
        Log4jCloseThread log4jCloseThread = new Log4jCloseThread();
        Runtime.getRuntime().addShutdownHook(log4jCloseThread);
        try {
            applicationContext = new AnnotationConfigApplicationContext(LogicApplication.class) {
                @Override
                protected void doClose() {
                    super.stop();
                    super.doClose();
                    LOCK.lock();
                    try {
                        stop.signalAll();
                    } finally {
                        LOCK.unlock();
                    }
                }
            };
            CONTEXT = applicationContext;
        } catch (Exception e) {
            log.error("初始化服务器应用上下文出错:{}", e.getMessage(), e);
            Runtime.getRuntime().exit(-1);
        }
        Runtime.getRuntime().removeShutdownHook(log4jCloseThread);
        applicationContext.registerShutdownHook();
        applicationContext.start();
        log.info("服务器已经启动 - [{}]", DateUtils.date2String(new Date(), DateUtils.PATTERN_DATE_TIME));

        LOCK.lock();
        try {
            stop.await();
        } catch (InterruptedException e) {
            log.info("主线程被interrupt唤醒，等待终止");
        } finally {
            LOCK.unlock();
        }
        log.info("服务器已经关闭 - [{}]", DateUtils.date2String(new Date(), DateUtils.PATTERN_DATE_TIME));
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
