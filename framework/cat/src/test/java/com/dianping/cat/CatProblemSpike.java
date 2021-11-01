package com.dianping.cat;

public class CatProblemSpike {
    public static void main(String[] args) throws InterruptedException {
        Cat.logError("message error", new RuntimeException("message error content"));
        Cat.logError(new IllegalStateException("illegal state"));
        Cat.logErrorWithCategory("long-url", new IllegalStateException("long-url-error"));
        Cat.logErrorWithCategory("long-sql", new IllegalStateException("long-sql-error"));
        Cat.logErrorWithCategory("long-call", new IllegalStateException("long-call-error"));
        Cat.logErrorWithCategory("long-cache", new IllegalStateException("long-cache-error"));

        Cat.logEvent("long-sql","Acount", "ERROR", "id=123");

        Thread.sleep(2000);
    }
}