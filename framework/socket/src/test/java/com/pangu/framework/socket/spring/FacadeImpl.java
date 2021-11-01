package com.pangu.framework.socket.spring;

import org.springframework.stereotype.Component;

@Component
public class FacadeImpl implements Facade {
    @Override
    public String say(String hello) {
        return "res:" + hello;
    }
}
