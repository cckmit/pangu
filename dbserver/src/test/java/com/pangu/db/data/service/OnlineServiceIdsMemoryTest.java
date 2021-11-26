package com.pangu.db.data.service;

import com.dianping.cat.status.jvm.MemoryInformation;
import com.pangu.framework.socket.utils.ServerIdGenerator;
import com.pangu.framework.utils.id.IdGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OnlineServiceIdsMemoryTest {

    public static void main(String[] args) throws InterruptedException {
        MemoryInformation preMemoryInformation = new MemoryInformation();
        long preUsedMemory = preMemoryInformation.getUsedMemory();
        System.out.println("preStar:" + preUsedMemory);
        Map<Long, Long> ids = new HashMap<>();
        ServerIdGenerator sessionGenerator = new ServerIdGenerator(1);
        IdGenerator idGenerator = new IdGenerator(1, 1, 1L);
        Thread.sleep(1000);
        System.gc();
        Thread.sleep(1000);
        MemoryInformation preOnline = new MemoryInformation();
        long preOnlineUsedMemory = preOnline.getUsedMemory();
        System.out.println("preOnline:" + preOnlineUsedMemory);
        int amount = 100_0000;
        for (int i = 0; i < amount; ++i) {
            long sessionId = sessionGenerator.getNext();
            long roleId = idGenerator.getNext();
            ids.put(sessionId, roleId);
        }
        Thread.sleep(1000);
        System.gc();
        Thread.sleep(1000);

        MemoryInformation afterOnline = new MemoryInformation();
        long afterOnlineMem = afterOnline.getUsedMemory();
        System.out.println("afterOnline:" + afterOnlineMem);
        System.out.println("online:" + ids.size() + ",costMem:" + (afterOnlineMem - preOnlineUsedMemory) / 1024 + "kb,perCost:" + (afterOnlineMem - preOnlineUsedMemory) / amount);
    }

}