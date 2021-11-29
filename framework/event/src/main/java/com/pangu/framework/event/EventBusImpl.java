package com.pangu.framework.event;

import com.dianping.cat.Cat;
import com.pangu.framework.utils.thread.NamedThreadFactory;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.pangu.framework.event.MonitorKey.EVENT;
import static com.pangu.framework.event.MonitorKey.EVENT_COUNT;

@Component
public class EventBusImpl implements EventBus, EventBusImplMBean, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(EventBusImpl.class);

    /**
     * 注册的事件接收者
     */
    private final ConcurrentHashMap<String, CopyOnWriteArraySet<Receiver<?>>> receivers = new ConcurrentHashMap<>();
    @Getter
    private final BlockingQueue<Event<?>> eventQueue = new LinkedBlockingQueue<Event<?>>();

    @Autowired(required = false)
    @Qualifier("event_queue_size")
    private final Integer queueSize = 10000;

    @Autowired(required = false)
    @Qualifier("event_pool_size")
    private final Integer poolSize = 5;
    @Autowired(required = false)
    @Qualifier("event_pool_max_size")
    private final Integer poolMaxSize = 10;
    @Autowired(required = false)
    @Qualifier("event_pool_alive_time")
    private final Integer poolKeepAlive = 60;
    @Autowired(required = false)
    @Qualifier("event_pool_await_time")
    private final Integer poolAwaitTime = 60;

    @Getter
    private static ThreadPoolExecutor pool;
    private static final AtomicBoolean initPool = new AtomicBoolean();
    private ObjectName jmxName;

    /**
     * 事件消费线程执行代码
     */
    private final Runnable consumerRunner = new Runnable() {

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                try {
                    Event<?> event = eventQueue.take();
                    String name = event.getName();
                    if (!receivers.containsKey(name)) {
                        logger.info("事件[{}]没有对应的接收器", name);
                        continue;
                    }
                    for (Receiver<?> receiver : receivers.get(name)) {
                        Runnable runner = createRunner(receiver, event);
                        try {
                            pool.submit(runner);
                        } catch (RejectedExecutionException e) {
                            logger.error("事件线程池已满，请尽快调整配置参数 - {}", name);
                            onRejected(receiver, event);
                        }
                    }
                } catch (InterruptedException e) {
                    logger.info("事件处理线程打断终止");
                    break;
                } catch (Throwable throwable) {
                    logger.error("****************************");
                    logger.error("事件处理遭遇严重异常", throwable);
                    logger.error("****************************");
                }
            }
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private void onRejected(Receiver receiver, Event event) {
            try {
                receiver.onEvent(event);
            } catch (ClassCastException e) {
                logger.error("事件[" + event.getName() + "]对象类型不符合接收器声明", e);
            } catch (Throwable t) {
                logger.error("事件[" + event.getName() + "]处理时发生异常", t);
            }
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private Runnable createRunner(final Receiver receiver, final Event event) {
            return new Runnable() {
                @Override
                public void run() {
                    try {
                        receiver.onEvent(event);
                    } catch (ClassCastException e) {
                        logger.error("事件[" + event.getName() + "]对象类型不符合接收器[" + receiver.getClass() + "]声明", e);
                    } catch (Throwable t) {
                        logger.error("事件[" + event.getName() + "]处理器[" + receiver.getClass() + "]运行时发生异常", t);
                    }
                }
            };
        }
    };
    private Thread consumerThread;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        initialize(applicationContext.getId());
    }

    /**
     * 根据配置初始化
     *
     * @param id
     */
    protected void initialize(String id) {
        ThreadGroup threadGroup = new ThreadGroup("事件模块");
        NamedThreadFactory threadFactory = new NamedThreadFactory(threadGroup, "事件处理");
        if (initPool.compareAndSet(false, true)) {
            pool = new ThreadPoolExecutor(poolSize, poolMaxSize, poolKeepAlive, TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(queueSize), threadFactory);
        }
        // 创建并启动事件消费线程
        consumerThread = new Thread(consumerRunner, "消费事件后台线程");
        consumerThread.setDaemon(true);
        consumerThread.start();
        EventMonitorCollector.register(this);
        // 注册MBean
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            id = StringUtils.replaceEachRepeatedly(id, new String[]{":", "*", "\n", "?"}, new String[]{"", "", "", ""});
            jmxName = new ObjectName("com.pangu.framework:type=EventBusMBean" + (id == null ? "" : "(" + id + ")"));
            mbs.registerMBean(this, jmxName);
        } catch (Exception e) {
            logger.error("注册[common-event]的JMX管理接口失败", e);
        }
    }

    /**
     * 销毁方法
     */
    @PreDestroy
    public void destory() {
        shutdown();
        if (this.jmxName != null) {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            try {
                mbs.unregisterMBean(this.jmxName);
            } catch (InstanceNotFoundException | MBeanRegistrationException e) {
                logger.error("unregister JMX error.");
            }
        }
        if (consumerThread != null) {
            consumerThread.interrupt();
        }
    }

    /**
     * 停止状态
     */
    private volatile boolean stop;

    /**
     * 关闭事件总线，阻塞方法会等待总线中的全部事件都发送完后再返回
     */
    public void shutdown() {
        if (isStop())
            return;
        stop = true;
        for (; ; ) {
            if (eventQueue.isEmpty()) {
                break;
            }
            Thread.yield();
        }
        // 等待线程池关闭
        pool.shutdown();
        logger.info("开始关闭事件总线线程池");
        try {
            if (!pool.awaitTermination(poolAwaitTime, TimeUnit.SECONDS)) {
                logger.error("无法在预计时间内完成事件总线线程池关闭,尝试强行关闭");
                pool.shutdownNow();
                if (!pool.awaitTermination(poolAwaitTime, TimeUnit.SECONDS)) {
                    logger.error("事件总线线程池无法完成关闭");
                }
            }
        } catch (InterruptedException e) {
            logger.error("事件总线线程池关闭时线程被打断,强制关闭事件总线线程池");
            pool.shutdownNow();
        }
        initPool.compareAndSet(true, false);
    }

    /**
     * 检查该事件总线是否已经停止服务
     *
     * @return
     */
    public boolean isStop() {
        return stop;
    }

    @Override
    public void post(Event<?> event) {
        if (event == null) {
            throw new IllegalArgumentException("事件对象不能为空");
        }
        if (stop) {
            throw new IllegalStateException("事件总线已经停止，不能再接收事件");
        }
        try {
            eventQueue.put(event);
        } catch (InterruptedException e) {
            logger.error("在添加事件对象时产生异常", e);
        }
        Cat.logMetricForCount(EVENT_COUNT);
        Cat.logEvent(EVENT, event.getName());
    }

    @Override
    public void register(String name, Receiver<?> receiver) {
        if (name == null || receiver == null) {
            throw new IllegalArgumentException("事件名和接收者均不能为空");
        }

        CopyOnWriteArraySet<Receiver<?>> set = receivers.get(name);
        if (set == null) {
            set = new CopyOnWriteArraySet<Receiver<?>>();
            CopyOnWriteArraySet<Receiver<?>> prev = receivers.putIfAbsent(name, set);
            set = prev != null ? prev : set;
        }

        set.add(receiver);
    }

    @Override
    public void unregister(String name, Receiver<?> receiver) {
        if (name == null || receiver == null) {
            throw new IllegalArgumentException("事件名和接收者均不能为空");
        }

        CopyOnWriteArraySet<Receiver<?>> set = receivers.get(name);
        if (set != null) {
            set.remove(receiver);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void syncPost(Event<?> event) {
        String name = event.getName();
        if (!receivers.containsKey(name)) {
            logger.warn("事件'{}'没有对应的接收器", name);
            return;
        }
        Cat.logMetricForCount(EVENT_COUNT);
        Cat.logEvent(EVENT, event.getName());
        for (Receiver receiver : receivers.get(name)) {
            try {
                receiver.onEvent(event);
            } catch (Exception e) {
                logger.error("事件[" + event.getName() + "]处理时发生异常", e);
            }
        }
    }

    public Map<String, CopyOnWriteArraySet<Receiver<?>>> getReceivers() {
        return receivers;
    }
    // JMX管理接口的实现方法

    @Override
    public int getEventQueueSize() {
        return eventQueue.size();
    }

    @Override
    public int getPoolActiveCount() {
        return pool.getActiveCount();
    }

    @Override
    public int getPollQueueSize() {
        return pool.getQueue().size();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<String> getEvents() {
        List<Event> dump = new ArrayList<Event>(eventQueue);
        ArrayList<String> result = new ArrayList<String>(dump.size());
        for (Event e : dump) {
            result.add(e.getName());
        }
        return result;
    }
}
