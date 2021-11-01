package com.dianping.cat;

import com.dianping.cat.message.Trace;

import java.util.Random;

public class CatCrossSpike {
    public static void main(String[] args) throws InterruptedException {
        Random random = new Random();
        for (int i = 0; i < 10; ++i) {
            Trace trace = Cat.newTrace("center", "arena");
            Thread.sleep(2000);

            Trace t2 = Cat.newTrace("center-server", "center-arena");
            Thread.sleep(2000);

            t2.addData("id=123");
            t2.setSuccessStatus();
            t2.complete();

            trace.addData("id=123");
            trace.setSuccessStatus();
            trace.complete();
        }
        Thread.sleep(1000);
    }
}
