package com.pangu.framework.socket.anno;

import com.pangu.framework.socket.handler.Session;

import java.lang.annotation.*;

/**
 * 添加此标识，必须要有管理后台标识 {@link Session#MANAGER}
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Manager {
}
