package  com.pangu.logic.utils;

import com.pangu.framework.utils.concurrent.DelayedElement;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.PriorityQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * 由于很多业务模块需要创建自己的定时延时队列，导致项目创建过多独立线程
 * 在使用单进程多项目的情况下，进程数量暴涨，导致线程上下文，以及线程CPU抢占严重
 */
@Slf4j
public class ProduceConsumer {

    private static final Queues queues = new Queues();
    private static final AtomicBoolean init = new AtomicBoolean();

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> CustomQueue<T> newDelay(Consumer<DelayedElement<T>> elementProcessor) {
        CustomQueue<T> customQueue = queues.add(elementProcessor);
        if (init.compareAndSet(false, true)) {
            Thread thread = new Thread(() -> {
                try {
                    while (true) {
                        CustomQueue cusQueue = queues.take();
                        DelayedElement<?> poll = cusQueue.poll();
                        if (poll == null) {
                            continue;
                        }
                        try {
                            cusQueue.processor.accept(poll);
                        } catch (Throwable thr) {
                            log.error("公共队列处理定时任务异常");
                        }
                    }
                } catch (InterruptedException e) {
                    log.info("公共线程直接终止");
                }
            }, "公共生产者消费者线程");
            thread.setDaemon(true);
            thread.start();
        }
        return customQueue;
    }

    public static class CustomQueue<T> {

        private final PriorityQueue<DelayedElement<T>> queue = new PriorityQueue<>();

        private final Consumer<DelayedElement<T>> processor;

        private final Queues queues;

        private final ReentrantLock lock = new ReentrantLock();

        CustomQueue(Consumer<DelayedElement<T>> processor, Queues queues) {
            this.processor = processor;
            this.queues = queues;
        }

        public void add(DelayedElement<T> element) {
            lock.lock();
            try {
                queue.add(element);
            } finally {
                lock.unlock();
            }
            queues.update();
        }

        DelayedElement<T> peek() {
            lock.lock();
            try {
                return queue.peek();
            } finally {
                lock.unlock();
            }
        }

        DelayedElement<T> poll() {
            lock.lock();
            try {
                return queue.poll();
            } finally {
                lock.unlock();
            }
        }

        public void clear() {
            lock.lock();
            try {
                queue.clear();
            } finally {
                lock.unlock();
            }
            queues.update();
        }

        public void put(DelayedElement<T> e) {
            add(e);
        }
    }

    static class Queues {
        private final CopyOnWriteArrayList<CustomQueue<?>> allCusQueue = new CopyOnWriteArrayList<>();
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition available = lock.newCondition();

        public <T> CustomQueue<T> add(Consumer<DelayedElement<T>> processor) {
            CustomQueue<T> queue = new CustomQueue<>(processor, this);
            allCusQueue.add(queue);
            return queue;
        }

        public void update() {
            lock.lock();
            try {
                available.signalAll();
            } finally {
                lock.unlock();
            }
        }

        public CustomQueue<?> take() throws InterruptedException {
            final ReentrantLock lock = this.lock;
            lock.lockInterruptibly();
            int delayTime = 60_000;
            try {
                for (; ; ) {
                    CustomQueue<?> queue = null;
                    Date minTime = null;
                    for (CustomQueue<?> item : allCusQueue) {
                        DelayedElement<?> element = item.peek();
                        if (element == null) {
                            continue;
                        }
                        Date end = element.getEnd();
                        if (minTime == null) {
                            minTime = end;
                            queue = item;
                            continue;
                        }
                        if (minTime.before(end)) {
                            continue;
                        }
                        minTime = end;
                        queue = item;
                    }
                    if (queue == null) {
                        available.await();
                    } else {
                        long delay = minTime.getTime() - System.currentTimeMillis();
                        if (delay > 0) {
                            available.await(delay < delayTime ? delay : delayTime, TimeUnit.MILLISECONDS);
                        } else {
                            return queue;
                        }
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }

}
