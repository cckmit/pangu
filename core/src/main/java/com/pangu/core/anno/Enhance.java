package com.pangu.core.anno;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Enhance {

    /**
     * 仅在返回该值时才更新
     */
    String value() default "";

    /**
     * 运行时抛出该异常时也会通知数据更新
     */
    Class<?> ignore() default void.class;
}
