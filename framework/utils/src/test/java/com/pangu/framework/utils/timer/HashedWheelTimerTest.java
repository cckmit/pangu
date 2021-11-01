package com.pangu.framework.utils.timer;

import org.junit.Test;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class HashedWheelTimerTest {
    @Test
    public void test_cap() {
        HashedWheelTimer hashedWheelTimer = new HashedWheelTimer(10, TimeUnit.MILLISECONDS, 10);
        Date now = new Date();
        int amount = 20;
        AtomicInteger count = new AtomicInteger();
        for (int i = 1; i <= amount; ++i) {
            hashedWheelTimer.newTimeout(timeout -> count.incrementAndGet(), i * 50, TimeUnit.MILLISECONDS);
        }
        while (hashedWheelTimer.pendingTimeouts() > 0) {
            Thread.yield();
        }
        assertEquals(amount, count.get());
    }
}