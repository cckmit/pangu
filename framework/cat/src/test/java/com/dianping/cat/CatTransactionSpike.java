package com.dianping.cat;

import com.dianping.cat.message.Transaction;

import java.util.Random;

public class CatTransactionSpike {
    public static void main(String[] args) throws InterruptedException {
        Random random = new Random();
        Transaction transaction = Cat.newTransaction("excep", "func1");
        for (int i = 0; i < 5; ++i) {
            Transaction t1 = Cat.newTransaction("excep", "fun1-2-" + i);
            t1.addData("command", String.valueOf(i));
            t1.addData("rout", "inter2");
            for (int j = 0; j < 2; ++j) {
                Transaction t2 = Cat.newTransaction("excep", "fun1-2-3-" + j);
                t2.addData("command", String.valueOf(i));
                t2.addData("rout", "inter2");
                Thread.sleep(random.nextInt(100));
                if (random.nextDouble() >= 0.8) {
                    t2.setStatus(new RuntimeException("this is error" + j));
                }
                t2.complete();
            }
            Thread.sleep(random.nextInt(100));
            t1.complete();
        }
        transaction.complete();
        Thread.sleep(2000);
    }
}