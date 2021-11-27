package com.pangu.framework.socket.anno;

import io.netty.util.AttributeKey;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface InSession {

    /**
     * {@link AttributeKey}的字符串表示格式
     * <pre>
     * [类名]@[键]
     * </pre>
     *
     * @return
     */
    String value();

    /**
     * 是否要求参数必须非空
     *
     * @return
     */
    boolean required() default true;

}
