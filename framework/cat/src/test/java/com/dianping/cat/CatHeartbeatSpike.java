package com.dianping.cat;

import com.dianping.cat.message.Heartbeat;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;

public class CatHeartbeatSpike {
    public static void main(String[] args) throws InterruptedException {
        Heartbeat h = Cat.getProducer().newHeartbeat("Cache", "server1");
        h.addData("entity=12345");
        h.setSuccessStatus();
        h.complete();

        Thread.sleep(200000000);
    }
}