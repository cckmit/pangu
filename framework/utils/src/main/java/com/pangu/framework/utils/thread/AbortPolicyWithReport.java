/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pangu.framework.utils.thread;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;

/**
 * Abort Policy.
 * Log warn info when abort.
 */
@Slf4j
public class AbortPolicyWithReport extends ThreadPoolExecutor.AbortPolicy {

    private final String threadName;

    private static volatile long lastPrintTime = 0;

    private static final long TEN_MINUTES_MILLS = 10 * 60 * 1000;

    private static final String OS_WIN_PREFIX = "win";

    private static final String OS_NAME_KEY = "os.name";

    private static final String WIN_DATETIME_FORMAT = "yyyy-MM-dd_HH-mm-ss";

    private static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd_HH:mm:ss";

    private static final Semaphore guard = new Semaphore(1);

    public AbortPolicyWithReport(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        String msg = String.format("Thread pool is EXHAUSTED!" +
                        " Thread Name: %s, Pool Size: %d (active: %d, core: %d, max: %d, largest: %d), Task: %d (completed: "
                        + "%d)," +
                        " Executor status:(isShutdown:%s, isTerminated:%s, isTerminating:%s)!",
                threadName, e.getPoolSize(), e.getActiveCount(), e.getCorePoolSize(), e.getMaximumPoolSize(),
                e.getLargestPoolSize(),
                e.getTaskCount(), e.getCompletedTaskCount(), e.isShutdown(), e.isTerminated(), e.isTerminating());
        log.warn(msg);
        if (!e.isShutdown()) {
            dumpJStack();
        }
        throw new RejectedExecutionException(msg);
    }

    private void dumpJStack() {
        long now = System.currentTimeMillis();

        //dump every 10 minutes
        if (now - lastPrintTime < TEN_MINUTES_MILLS) {
            return;
        }

        if (!guard.tryAcquire()) {
            return;
        }
        if (now - lastPrintTime < TEN_MINUTES_MILLS) {
            return;
        }
        ExecutorService pool = Executors.newSingleThreadExecutor();
        pool.execute(() -> {
            Path path = Paths.get(".");

            SimpleDateFormat sdf;

            String os = System.getProperty(OS_NAME_KEY).toLowerCase();

            // window system don't support ":" in file name
            if (os.contains(OS_WIN_PREFIX)) {
                sdf = new SimpleDateFormat(WIN_DATETIME_FORMAT);
            } else {
                sdf = new SimpleDateFormat(DEFAULT_DATETIME_FORMAT);
            }

            String dateStr = sdf.format(new Date());
            //try-with-resources
            File file = path.resolve("Common_JStack.log" + "." + dateStr).toFile();
            try (FileOutputStream jStackStream = new FileOutputStream(file)) {
                JVMUtil.jstack(jStackStream);
                log.warn("任务拒绝线程dump成功[" + file.getAbsolutePath() + "]");
            } catch (Throwable t) {
                log.error("dump jStack error", t);
            } finally {
                lastPrintTime = System.currentTimeMillis();
                guard.release();
            }
        });
        //must shutdown thread pool ,if not will lead to OOM
        pool.shutdown();

    }

}
