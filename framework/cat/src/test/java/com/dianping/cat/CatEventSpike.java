package com.dianping.cat;

import com.dianping.cat.message.Event;

import java.util.Random;

public class CatEventSpike {
    public static void main(String[] args) throws InterruptedException {
        Random random = new Random();
        Event event = Cat.newEvent("excep-sub", "func1");
        for (int i = 0; i < 5; ++i) {
            Event t1 = Cat.newEvent("excep", "fun1-2-" + i);
            t1.addData("command", String.valueOf(i));
            t1.addData("rout", "inter2");
            for (int j = 0; j < 2; ++j) {
                Cat.logEvent("excep", "fun1-2-3-" + j);
            }
            Thread.sleep(random.nextInt(100));
            t1.complete();
        }
        event.complete();
        Thread.sleep(2000);
    }
}