package com.dianping.cat;

import java.util.HashMap;
import java.util.Random;

public class CatMetricSpike {
    public static void main(String[] args) throws InterruptedException {
        Random random = new Random();
        for (int i = 0; i < 10; ++i) {
            Cat.logMetricForCount("register_amount", 10);
            HashMap<String, String> metric = new HashMap<>();
            metric.put("day1", "10");
            metric.put("day2", "20");
            Cat.logMetricForCount("login", metric);
            Cat.logMetricForDuration("logout", 500 + random.nextInt(3000));
            Thread.sleep(2000);
        }
        Thread.sleep(1000);
    }
}
