package com.pangu.framework.socket.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface InBody {

    /**
     * 值来源配置
     * <ul>
     * <li>POJO对象的属性名</li>
     * <li>数组的元素下标</li>
     * </ul>
     *
     * @return
     */
    String value() default "";

    /**
     * 是否要求参数必须非空
     *
     * @return
     */
    boolean required() default true;
}
