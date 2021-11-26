package com.pangu.db.data.service;

import com.pangu.framework.socket.utils.ServerIdGenerator;
import com.pangu.framework.utils.collection.ConcurrentHashSet;
import com.pangu.framework.utils.id.IdGenerator;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

import static org.junit.Assert.assertEquals;

public class OnlineServiceTest {
    @Test
    public void test_online_offline() {
        OnlineService onlineService = new OnlineService(null);
        int thread = 5;
        int amount = 100_0000;
        CountDownLatch latch = new CountDownLatch(thread);
        CyclicBarrier barrier = new CyclicBarrier(thread);
        long start = System.currentTimeMillis();
        for (int i = 1; i <= thread; i++) {
            ServerIdGenerator sessionGenerator = new ServerIdGenerator(i);
            IdGenerator idGenerator = new IdGenerator(1, i, 1L);
            Thread t = new Thread(() -> {
                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
                for (int l = 0; l < amount; ++l) {
                    long sessionId = sessionGenerator.getNext();
                    long roleId = idGenerator.getNext();
                    onlineService.online(sessionId, roleId);
                    onlineService.offline(sessionId, roleId);
                }
                latch.countDown();
            });
            t.setDaemon(true);
            t.start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println((System.currentTimeMillis() - start) + "ms");
        ConcurrentHashMap<Long, Long> roleSession = onlineService.getRoleSession();
        assertEquals(0, roleSession.size());
        ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, ConcurrentHashSet<Long>>> oidSidRole = onlineService.getOidSidRole();
        for (Map.Entry<Integer, ConcurrentHashMap<Integer, ConcurrentHashSet<Long>>> entry : oidSidRole.entrySet()) {
            ConcurrentHashMap<Integer, ConcurrentHashSet<Long>> v = entry.getValue();
            for (Map.Entry<Integer, ConcurrentHashSet<Long>> e : v.entrySet()) {
                ConcurrentHashSet<Long> n = e.getValue();
                assertEquals(0, n.size());
            }
        }
        assertEquals(0, onlineService.getSessionRole().size());
        ConcurrentHashMap<Integer, ConcurrentHashSet<Long>> gateRoleIds = onlineService.getGateRoleIds();
        for (Map.Entry<Integer, ConcurrentHashSet<Long>> entry : gateRoleIds.entrySet()) {
            ConcurrentHashSet<Long> v = entry.getValue();
            assertEquals(0, v.size());
        }
    }

}