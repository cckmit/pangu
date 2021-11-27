package com.pangu.framework.socket.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * [1:符号位预留][32:秒][6:服务器编号][25:自增]
 */
public class ServerIdGenerator {

    private final static long SEC_LEN = 32;
    private final static long SEC_MASK = (1L << SEC_LEN) - 1;
    private final static long SERVER_LEN = 6;
    private final static long SERVER_MASK = (1 << SERVER_LEN) - 1;
    private final static long GENERATOR_LEN = 25;
    private final static long GENERATOR_MASK = (1 << GENERATOR_LEN) - 1;
    private final AtomicInteger generator = new AtomicInteger(1);
    private long serverId;

    public ServerIdGenerator() {
    }

    public ServerIdGenerator(int serverId) {
        this.serverId = serverId;
    }

    public static int toServerId(long id) {
        return (int) ((id >> GENERATOR_LEN) & SERVER_MASK);
    }

    public long getNext() {
        long curSec = System.currentTimeMillis() / 1000;
        return ((curSec & SEC_MASK) << (SERVER_LEN + GENERATOR_LEN)) | ((serverId & SERVER_MASK) << GENERATOR_LEN) | (generator.incrementAndGet() & GENERATOR_MASK);
    }

    public void setServerId(long serverId) {
        this.serverId = serverId & SERVER_MASK;
    }
}
