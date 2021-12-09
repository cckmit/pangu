package com.pangu.logic.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.pangu.core.common.Constants;
import com.pangu.core.common.ServerInfo;
import com.pangu.core.common.ZookeeperTask;
import com.pangu.framework.utils.json.JsonUtils;
import com.pangu.framework.utils.math.RandomUtils;
import com.pangu.logic.utils.TaskQueueSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.recipes.queue.DistributedQueue;
import org.apache.curator.framework.recipes.queue.QueueBuilder;
import org.apache.curator.framework.recipes.queue.QueueConsumer;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.RetryForever;
import org.apache.zookeeper.data.Stat;
import org.checkerframework.checker.units.qual.A;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

import static com.pangu.core.common.Constants.TASK_CREATE_GAME_DATABASE;

@Slf4j
public class CuratorMessageQueueSpy {
    public static void main(String[] args) throws Exception {
        String path = "/pangu_test/queue_performance";
        Consumer consumer = new Consumer(path);
        Producer producer = new Producer(path);
        int count = 1_0000;
        for (int i = 0; i < count; ++i) {
            producer.produce();
        }
        Thread.sleep(10_0000);

    }

    public static class Producer {

        private final CuratorFramework framework;
        private final DistributedQueue<ZookeeperTask> queue;
        private AtomicLong id = new AtomicLong();

        public Producer(String path) throws Exception {
            framework = CuratorFrameworkFactory.builder()
                    .connectString("localhost:2181")
                    .sessionTimeoutMs(20_000)
                    .connectionTimeoutMs(10_000)
                    .retryPolicy(new RetryForever(3000))
                    .build();
            framework.start();
            queue = QueueBuilder.builder(framework, null, new TaskQueueSerializer(), path).buildQueue();
            queue.start();
        }

        public void produce() throws Exception {
            queue.put(new ZookeeperTask(String.valueOf(id.incrementAndGet()), String.valueOf(System.currentTimeMillis())));
        }
    }

    public static class Consumer {

        private final CuratorFramework framework;

        public Consumer(String path) throws Exception {
            framework = CuratorFrameworkFactory.builder()
                    .connectString("localhost:2181")
                    .sessionTimeoutMs(20_000)
                    .connectionTimeoutMs(10_000)
                    .retryPolicy(new RetryForever(3000))
                    .build();
            framework.start();

            DistributedQueue<ZookeeperTask> queue = QueueBuilder.builder(framework, new QueueConsumer<ZookeeperTask>() {

                final LongAdder count = new LongAdder();
                final LongAdder delay = new LongAdder();

                {
                    Thread t = new Thread(() -> {
                        while (true) {
                            long c = count.sumThenReset();
                            long avg = 0;
                            if (c > 0) {
                                avg = delay.sumThenReset() / c;
                            }
                            System.out.println(new Date() + ",count:" + c + ",delay:" + avg);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    t.setDaemon(true);
                    t.start();
                }

                @Override
                public void consumeMessage(ZookeeperTask message) {
                    long time = Long.parseLong(message.getParams());
                    long delay = System.currentTimeMillis() - time;
                    this.count.increment();
                    this.delay.add(delay);
                }

                @Override
                public void stateChanged(CuratorFramework client, ConnectionState newState) {

                }
            }, new TaskQueueSerializer(), path).buildQueue();
            queue.start();
        }
    }
}
