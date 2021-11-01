package com.pangu.framework.socket.monitor;

import com.dianping.cat.status.StatusExtension;
import com.dianping.cat.status.StatusExtensionRegister;
import com.pangu.framework.socket.handler.Dispatcher;
import com.pangu.framework.socket.handler.SyncSupport;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadPoolExecutor;

public class SocketCatCollector {

    public static void register(Dispatcher dispatcher) {
        StatusExtensionRegister instance = StatusExtensionRegister.getInstance();
        instance.register(new StatusExtension() {
            @Override
            public String getDescription() {
                return "Socket Dispatcher Thread";
            }

            @Override
            public String getId() {
                return "socket.thread";
            }

            @Override
            public Map<String, String> getProperties() {
                ThreadPoolExecutor[] messagePoolExecutors = dispatcher.getMessagePoolExecutors();
                int maxQueue = 0;
                for (ThreadPoolExecutor poolExecutor : messagePoolExecutors) {
                    BlockingQueue<Runnable> queue = poolExecutor.getQueue();
                    int size = queue.size();
                    maxQueue = Math.max(size, maxQueue);
                }
                Map<String, String> values = new HashMap<>(3);
                values.put("socket.thread.client", String.valueOf(maxQueue));

                ThreadPoolExecutor managedExecutorService = dispatcher.getManagedExecutorService();
                if (managedExecutorService != null) {
                    BlockingQueue<Runnable> queue = managedExecutorService.getQueue();
                    values.put("socket.thread.manage", String.valueOf(queue.size()));
                }

                SyncSupport syncSupport = dispatcher.getSyncSupport();
                ConcurrentMap<String, ThreadPoolExecutor> threads = syncSupport.getThreads();
                for (Map.Entry<String, ThreadPoolExecutor> entry : threads.entrySet()) {
                    String name = entry.getKey();
                    ThreadPoolExecutor poolExecutor = entry.getValue();
                    BlockingQueue<Runnable> curQueue = poolExecutor.getQueue();
                    values.put("socket.thread.sync." + name, String.valueOf(curQueue.size()));
                }
                return values;
            }
        });
    }
}
