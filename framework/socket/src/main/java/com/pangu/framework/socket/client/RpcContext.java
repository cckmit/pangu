package com.pangu.framework.socket.client;

import java.util.concurrent.CompletableFuture;

public class RpcContext {
    private final static ThreadLocal<CompletableFuture<?>> futures = new ThreadLocal<>();

    public static <T> CompletableFuture<T> get() {
        return (CompletableFuture<T>) futures.get();
    }

    public static void set(CompletableFuture<?> future) {
        futures.set(future);
    }

    public static void unset() {
        futures.remove();
    }
}
