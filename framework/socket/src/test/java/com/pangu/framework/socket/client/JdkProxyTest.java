package com.pangu.framework.socket.client;

import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.ExecutionException;

public class JdkProxyTest {

    @Test(expected = UndeclaredThrowableException.class)
    public void test_invoke() {

        HelloService helloService = (HelloService) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{HelloService.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String arg = (String) args[0];
                if (arg != null) {
                    throw new ExecutionException("", new IllegalArgumentException());
                }
                return null;
            }
        });
        helloService.say("hello");
    }

    private interface HelloService {
        void say(String hello);
    }
}
