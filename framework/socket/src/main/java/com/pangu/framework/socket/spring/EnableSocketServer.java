package com.pangu.framework.socket.spring;


import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ServerSpringConfiguration.class)
public @interface EnableSocketServer {
}
