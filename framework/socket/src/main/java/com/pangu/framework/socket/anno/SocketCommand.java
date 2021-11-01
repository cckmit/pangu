package com.pangu.framework.socket.anno;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SocketCommand {

    /**
     * 指令值
     *
     * @return
     */
    int value();


    /**
     * 标识消息体是否使用字节数组类型
     *
     * @return
     */
    Raw raw() default @Raw();
}
