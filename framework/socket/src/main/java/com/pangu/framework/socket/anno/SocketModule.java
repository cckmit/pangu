package com.pangu.framework.socket.anno;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SocketModule {

    /**
     * 模块声明
     *
     * @return
     */
    int value();
}
