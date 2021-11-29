package com.pangu.framework.event;

import com.dianping.cat.status.StatusExtension;
import com.dianping.cat.status.StatusExtensionRegister;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static com.pangu.framework.event.MonitorKey.EVENT_CURRENT_COUNT;
import static com.pangu.framework.event.MonitorKey.EVENT_POOL_COUNT;

public class EventMonitorCollector {

    public static void register(EventBusImpl eventBus) {
        StatusExtensionRegister instance = StatusExtensionRegister.getInstance();
        instance.register(new StatusExtension() {

            @Override
            public String getDescription() {
                return "common event";
            }

            @Override
            public String getId() {
                return "common.event";
            }

            @Override
            public Map<String, String> getProperties() {

                BlockingQueue<Event<?>> eventQueue = eventBus.getEventQueue();
                Map<String, String> values = new HashMap<>(2);
                values.put(EVENT_CURRENT_COUNT, String.valueOf(eventQueue.size()));
                ThreadPoolExecutor pool = EventBusImpl.getPool();
                if(pool!=null) {
                    values.put(EVENT_POOL_COUNT, String.valueOf(pool.getQueue().size()));
                }
                return values;
            }
        });
    }
}
