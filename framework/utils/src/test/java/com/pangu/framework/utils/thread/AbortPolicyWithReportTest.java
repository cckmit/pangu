package com.pangu.framework.utils.thread;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;

public class AbortPolicyWithReportTest {
    @Before
    public void setUp() {
        deleteDumpFile();
    }

    @After
    public void tearDown() {
        deleteDumpFile();
    }

    private void deleteDumpFile() {
        Path path = Paths.get(".");
        File file = path.toFile();
        File[] files = file.listFiles();
        if (files == null) {
            return;
        }
        for (File cur : files) {
            String name = cur.getName();
            if (name.startsWith("Common_JStack.log")) {
                cur.delete();
            }
        }
    }

    @Test
    public void test_rejectedExecution() {
        AbortPolicyWithReport policy = new AbortPolicyWithReport("测试");
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        try {
            policy.rejectedExecution(() -> {
            }, executor);
        } catch (RejectedExecutionException ignore) {

        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test_mul_thread_reject_execution() {
        AbortPolicyWithReport policy = new AbortPolicyWithReport("测试");
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        int count = 10;
        CyclicBarrier cyclicBarrier = new CyclicBarrier(count);
        for (int i = 0; i <= count; ++i) {
            Thread thread = new Thread(() -> {
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
                try {
                    policy.rejectedExecution(() -> {
                    }, executor);
                } catch (RejectedExecutionException ignore) {

                }
            });
            thread.setDaemon(true);
            thread.start();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Path path = Paths.get(".");
        File file = path.toFile();
        File[] files = file.listFiles();
        if (files == null) {
            return;
        }
        int dumpCount = 0;
        for (File cur : files) {
            String name = cur.getName();
            if (name.startsWith("Common_JStack.log")) {
                ++dumpCount;
            }
        }
        assertEquals(1, dumpCount);
    }
}