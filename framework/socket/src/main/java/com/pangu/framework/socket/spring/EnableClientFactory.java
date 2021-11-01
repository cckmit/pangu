package com.pangu.framework.socket.spring;


import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ClientSpringConfiguration.class)
public @interface EnableClientFactory {
}
