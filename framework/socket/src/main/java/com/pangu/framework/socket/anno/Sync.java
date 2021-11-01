package com.pangu.framework.socket.anno;

import java.lang.annotation.*;

/**
 * 针对服务端调用，所有同一个请求队列
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Sync {
    String value();
}
