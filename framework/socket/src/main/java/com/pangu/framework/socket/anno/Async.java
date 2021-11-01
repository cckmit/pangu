package com.pangu.framework.socket.anno;

import java.lang.annotation.*;

/**
 * 针对客户端 Interface 异步调用，方法返回值将会设置为null
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Async {
}
