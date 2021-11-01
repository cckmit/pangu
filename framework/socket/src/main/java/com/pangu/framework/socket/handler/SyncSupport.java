package com.pangu.framework.socket.handler;

import com.pangu.framework.utils.thread.AbortPolicyWithReport;
import com.pangu.framework.utils.thread.NamedThreadFactory;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.*;


public class SyncSupport {

    @Getter
    private final ConcurrentMap<String, ThreadPoolExecutor> threads = new ConcurrentHashMap<>();

    private final ThreadFactory threadFactory = new NamedThreadFactory("同步处理线程");

    public void run(String key, Runnable runnable) {
        AbortPolicyWithReport policy = new AbortPolicyWithReport("同步处理线程");
        ThreadPoolExecutor executor = threads.computeIfAbsent(key, k -> new ThreadPoolExecutor(0,
                1,
                5,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1000),
                threadFactory, policy));
        executor.submit(runnable);
    }

    public void shutdown() {
        for (Map.Entry<String, ThreadPoolExecutor> entry : threads.entrySet()) {
            ThreadPoolExecutor v = entry.getValue();
            v.shutdown();
        }
    }
}
