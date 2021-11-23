package com.pangu.framework.protocol.spring;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({})
@Retention(RUNTIME)
public @interface ProtocolClass {
    Class<?> clz();

    int index();
}
